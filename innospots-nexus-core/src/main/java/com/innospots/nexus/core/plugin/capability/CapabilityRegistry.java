package com.innospots.nexus.core.plugin.capability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.lifecycle.PluginAvailabilityIndex;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过写时复制向并发读者提供完整不可变快照的 Capability 注册表。
 *
 * <p>读操作无锁；写操作通过同步方法原子发布新快照。查询方法返回的 Provider
 * 实例由所属插件拥有，调用方不得跨插件生命周期持有。</p>
 */
public final class CapabilityRegistry implements CapabilityManager {

    private static final Logger logger = LoggerFactory.getLogger(CapabilityRegistry.class);

    private final AtomicReference<Map<CapabilityKey, List<CapabilityRegistration<?>>>> snapshot =
            new AtomicReference<>(Map.of());
    private final CapabilityRouter router;
    private final PluginAvailabilityIndex availabilityIndex;

    public CapabilityRegistry(Map<CapabilityKey, Tags> defaultRoutes) {
        this(defaultRoutes, null);
    }

    /**
     * 使用配置的默认路由和可用性索引创建注册表。
     *
     * @param defaultRoutes      查询未指定标签时使用的路由标签
     * @param availabilityIndex  插件可用性索引；为 {@code null} 时不做门控（单元测试）
     */
    public CapabilityRegistry(Map<CapabilityKey, Tags> defaultRoutes, PluginAvailabilityIndex availabilityIndex) {
        this.router = new CapabilityRouter(defaultRoutes);
        this.availabilityIndex = availabilityIndex;
    }

    /**
     * 原子发布已成功启动插件提供的全部注册记录。
     *
     * @param registrations 作为一个快照发布的 Provider 注册记录
     * @throws NexusException 注册记录非法或违反默认路由时抛出
     */
    public synchronized void registerAll(List<CapabilityRegistration<?>> registrations) {
        if (registrations == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability registrations must not be null");
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> mutable = mutableCopy(snapshot.get());
        for (CapabilityRegistration<?> registration : registrations) {
            if (registration == null) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "capability registration must not be null");
            }
            List<CapabilityRegistration<?>> current = mutable.computeIfAbsent(
                    registration.type().key(),
                    ignored -> new ArrayList<>());
            for (CapabilityRegistration<?> existing : current) {
                if (existing.type().api() != registration.type().api()) {
                    throw NexusException.build(
                            PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                            "different API classes registered for " + registration.type().key());
                }
            }
            current.add(registration);
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> replacement = immutableCopy(mutable);
        router.validateDefaults(replacement);
        snapshot.set(replacement);
        if (!registrations.isEmpty()) {
            String pluginId = registrations.getFirst().pluginId();
            logger.info("Registered {} capability provider(s) for plugin {}",
                    registrations.size(), pluginId);
        }
    }

    /**
     * 原子移除一个插件拥有的全部注册记录。
     *
     * @param pluginId 稳定的归属插件标识
     * @throws NexusException 归属标识为空时抛出
     */
    public synchronized void unregisterPlugin(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "plugin id must not be blank");
        }
        Map<CapabilityKey, List<CapabilityRegistration<?>>> mutable = mutableCopy(snapshot.get());
        mutable.replaceAll((key, values) -> values.stream()
                .filter(value -> !value.pluginId().equals(pluginId))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        mutable.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        snapshot.set(immutableCopy(mutable));
        logger.info("Unregistered all capability providers for plugin {}", pluginId);
    }

    /**
     * 返回指定 Capability 是否至少存在一个活动 Provider。
     *
     * @param key Capability 逻辑身份
     * @return 是否存在至少一个已注册 Provider
     * @throws NexusException 键为空时抛出
     */
    public boolean contains(CapabilityKey key) {
        if (key == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability key must not be null");
        }
        return snapshot.get().getOrDefault(key, List.of()).stream()
                .anyMatch(this::isVisible);
    }

    /**
     * 判断当前活动注册中是否存在匹配标签的 Provider。
     *
     * @param key Capability 逻辑身份
     * @param requiredTags 必须全部匹配的路由标签
     * @return 是否存在至少一个匹配 Provider
     * @throws NexusException 键或标签为空时抛出
     */
    public boolean contains(CapabilityKey key, Tags requiredTags) {
        if (key == null || requiredTags == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability key and required tags must not be null");
        }
        return snapshot.get().getOrDefault(key, List.of()).stream()
                .filter(this::isVisible)
                .anyMatch(registration -> registration.tags().matches(requiredTags));
    }

    @Override
    public <T extends CapabilityProvider> T require(String name, int majorVersion, Tags requiredTags) {
        return require(resolveType(name, majorVersion), requiredTags);
    }

    @Override
    public <T extends CapabilityProvider> T require(CapabilityType<T> type, Tags requiredTags) {
        return find(type, requiredTags).orElseThrow(() -> NexusException.build(
                PluginStatusCode.CAPABILITY_NOT_FOUND,
                "capability not found: " + type.key()));
    }

    @Override
    public <T extends CapabilityProvider> Optional<T> find(String name, int majorVersion, Tags requiredTags) {
        return find(resolveType(name, majorVersion), requiredTags);
    }

    @Override
    public <T extends CapabilityProvider> Optional<T> find(CapabilityType<T> type, Tags requiredTags) {
        List<CapabilityRegistration<T>> registrations = registrations(type);
        CapabilityRegistration<T> selected = router.select(type, requiredTags, registrations);
        return selected == null ? Optional.empty() : Optional.of(selected.provider());
    }

    @Override
    public <T extends CapabilityProvider> List<T> findAll(String name, int majorVersion) {
        return findAll(resolveType(name, majorVersion));
    }

    @Override
    public <T extends CapabilityProvider> List<T> findAll(CapabilityType<T> type) {
        return registrations(type).stream().map(CapabilityRegistration::provider).toList();
    }

    /**
     * 返回用于依赖解析和诊断计算的不可变注册快照。
     *
     * @return Capability 到注册记录的不可变快照
     */
    public Map<CapabilityKey, List<CapabilityRegistration<?>>> snapshot() {
        return snapshot.get();
    }

    private <T extends CapabilityProvider> CapabilityType<T> resolveType(String name, int majorVersion) {
        CapabilityKey key = new CapabilityKey(name, majorVersion);
        List<CapabilityRegistration<?>> registrations = snapshot.get().getOrDefault(key, List.of());
        if (registrations.isEmpty()) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_NOT_FOUND,
                    "capability not found: " + key);
        }
        CapabilityType<?> type = registrations.getFirst().type();
        for (CapabilityRegistration<?> registration : registrations) {
            if (registration.type().api() != type.api()) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "registered API does not match requested type: " + key);
            }
        }
        return castType(type);
    }

    @SuppressWarnings("unchecked")
    private static <T extends CapabilityProvider> CapabilityType<T> castType(CapabilityType<?> type) {
        return (CapabilityType<T>) type;
    }

    private <T extends CapabilityProvider> List<CapabilityRegistration<T>> registrations(CapabilityType<T> type) {
        if (type == null) {
            throw NexusException.build(
                    PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                    "capability type must not be null");
        }
        List<CapabilityRegistration<?>> raw = snapshot.get().getOrDefault(type.key(), List.of());
        List<CapabilityRegistration<T>> typed = new ArrayList<>(raw.size());
        for (CapabilityRegistration<?> registration : raw) {
            if (!isVisible(registration)) {
                continue;
            }
            if (registration.type().api() != type.api() || !type.api().isInstance(registration.provider())) {
                throw NexusException.build(
                        PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "registered API does not match requested type: " + type.key());
            }
            typed.add(cast(type, registration));
        }
        return List.copyOf(typed);
    }

    private static <T extends CapabilityProvider> CapabilityRegistration<T> cast(
            CapabilityType<T> type,
            CapabilityRegistration<?> registration
    ) {
        return new CapabilityRegistration<>(
                type,
                type.api().cast(registration.provider()),
                registration.providerRef(),
                registration.tags());
    }

    private static Map<CapabilityKey, List<CapabilityRegistration<?>>> mutableCopy(
            Map<CapabilityKey, List<CapabilityRegistration<?>>> source
    ) {
        Map<CapabilityKey, List<CapabilityRegistration<?>>> copy = new LinkedHashMap<>();
        source.forEach((key, values) -> copy.put(key, new ArrayList<>(values)));
        return copy;
    }

    private static Map<CapabilityKey, List<CapabilityRegistration<?>>> immutableCopy(
            Map<CapabilityKey, List<CapabilityRegistration<?>>> source
    ) {
        Map<CapabilityKey, List<CapabilityRegistration<?>>> copy = new LinkedHashMap<>();
        source.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        return Map.copyOf(copy);
    }

    private boolean isVisible(CapabilityRegistration<?> registration) {
        if (availabilityIndex == null) {
            return true;
        }
        return availabilityIndex.isVisible(registration.pluginId());
    }
}

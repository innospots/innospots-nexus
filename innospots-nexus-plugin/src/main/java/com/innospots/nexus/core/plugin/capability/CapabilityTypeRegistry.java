package com.innospots.nexus.core.plugin.capability;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.contract.CapabilityProvider;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Capability 逻辑身份到 Java API 的映射表，不注册 Provider 实现类。
 *
 * <p>发现阶段由插件声明自动填充；构建后不可变，可在多个运行时实例间安全共享。</p>
 */
public final class CapabilityTypeRegistry {

    private final Map<CapabilityKey, CapabilityType<?>> types;

    private CapabilityTypeRegistry(Map<CapabilityKey, CapabilityType<?>> types) {
        this.types = Map.copyOf(types);
    }

    /**
     * 创建注册表构建器。
     *
     * @return 新的可变构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 按 type@major 查询已注册 API。
     *
     * @param key Capability 逻辑身份
     * @return 已注册类型；未注册时返回空 Optional
     */
    public Optional<CapabilityType<?>> find(CapabilityKey key) {
        return Optional.ofNullable(types.get(key));
    }

    /**
     * 按名称和主版本查询已注册 API。
     *
     * @param name 小写点分 Capability 名称
     * @param majorVersion 正整数形式的 API 主版本
     * @return 已注册类型；未注册时返回空 Optional
     */
    public Optional<CapabilityType<?>> find(String name, int majorVersion) {
        return find(new CapabilityKey(name, majorVersion));
    }

    /**
     * 返回不可变的完整注册快照。
     *
     * @return Capability 身份到类型的不可变映射
     */
    public Map<CapabilityKey, CapabilityType<?>> snapshot() {
        return types;
    }

    /** 发现阶段累积 Capability API 的构建器。 */
    public static final class Builder {

        private final Map<CapabilityKey, CapabilityType<?>> types = new LinkedHashMap<>();

        /**
         * 按名称和主版本查询已登记 API。
         *
         * @param name 小写点分 Capability 名称
         * @param majorVersion 正整数形式的 API 主版本
         * @return 已登记类型；未登记时返回空 Optional
         */
        public Optional<CapabilityType<?>> find(String name, int majorVersion) {
            return Optional.ofNullable(types.get(new CapabilityKey(name, majorVersion)));
        }

        /**
         * 从插件定义登记其声明的全部 Capability 类型。
         *
         * @param definition 已校验的插件定义
         * @return 当前构建器
         */
        public Builder registerFrom(PluginDefinition definition) {
            if (definition == null) {
                throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                        "plugin definition is required");
            }
            for (CapabilityContribution<?> capability : definition.capabilities()) {
                register(capability.type());
            }
            return this;
        }

        /**
         * 登记一个 Capability 类型，重复 key 但 API 不同会被拒绝。
         *
         * @param type 要登记的 Capability 类型
         * @return 当前构建器
         * @throws NexusException 类型为空或同一 key 登记了不同 API 时抛出
         */
        public <T extends CapabilityProvider> Builder register(CapabilityType<T> type) {
            if (type == null) {
                throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                        "capability type is required");
            }
            CapabilityType<?> previous = types.putIfAbsent(type.key(), type);
            if (previous != null && previous.api() != type.api()) {
                throw NexusException.build(PluginStatusCode.CAPABILITY_TYPE_MISMATCH,
                        "different API classes registered for " + type.key());
            }
            return this;
        }

        /**
         * 构建不可变注册表。
         *
         * @return 冻结后的注册表快照
         */
        public CapabilityTypeRegistry build() {
            return new CapabilityTypeRegistry(new LinkedHashMap<>(types));
        }
    }
}

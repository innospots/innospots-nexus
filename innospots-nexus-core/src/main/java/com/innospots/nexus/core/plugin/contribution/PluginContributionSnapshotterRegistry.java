package com.innospots.nexus.core.plugin.contribution;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 宿主显式注册的安全 Contribution 快照器表。
 *
 * <p>构建后不可变，可在多个映射器或持久化路径间安全共享。</p>
 */
public final class PluginContributionSnapshotterRegistry {

    private final Map<PluginContributionType<?>, PluginContributionSnapshotter<?>> snapshotters;

    private PluginContributionSnapshotterRegistry(
            Map<PluginContributionType<?>, PluginContributionSnapshotter<?>> snapshotters
    ) {
        this.snapshotters = Map.copyOf(snapshotters);
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
     * 查找类型对应的快照器。
     *
     * @param type Contribution 类型标识
     * @return 已注册快照器；未注册时返回空 Optional
     */
    public Optional<PluginContributionSnapshotter<?>> find(PluginContributionType<?> type) {
        return Optional.ofNullable(snapshotters.get(type));
    }

    /**
     * 返回不可变快照器快照。
     *
     * @return 类型到快照器的不可变映射
     */
    public Map<PluginContributionType<?>, PluginContributionSnapshotter<?>> snapshot() {
        return snapshotters;
    }

    /** 快照器注册构建器。 */
    public static final class Builder {

        private final Map<PluginContributionType<?>, PluginContributionSnapshotter<?>> snapshotters = new LinkedHashMap<>();

        /**
         * 注册一个快照器。
         *
         * @param snapshotter 要注册的快照器
         * @return 当前构建器
         * @throws NexusException 快照器或其类型为空，或同一类型注册了不同实例时抛出
         */
        public <T extends PluginContribution> Builder register(PluginContributionSnapshotter<T> snapshotter) {
            if (snapshotter == null || snapshotter.type() == null) {
                throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                        "contribution snapshotter is required");
            }
            PluginContributionSnapshotter<?> previous = snapshotters.putIfAbsent(snapshotter.type(), snapshotter);
            if (previous != null && previous != snapshotter) {
                throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                        "duplicate contribution snapshotter: " + snapshotter.type());
            }
            return this;
        }

        /**
         * 构建不可变注册表。
         *
         * @return 冻结后的注册表快照
         */
        public PluginContributionSnapshotterRegistry build() {
            return new PluginContributionSnapshotterRegistry(new LinkedHashMap<>(snapshotters));
        }
    }
}

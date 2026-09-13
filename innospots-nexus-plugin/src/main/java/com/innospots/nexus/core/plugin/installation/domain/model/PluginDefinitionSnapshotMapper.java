package com.innospots.nexus.core.plugin.installation.domain.model;

import java.util.List;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.core.plugin.capability.Tags;
import com.innospots.nexus.core.plugin.contribution.PluginContribution;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotter;
import com.innospots.nexus.core.plugin.contribution.PluginContributionSnapshotterRegistry;
import com.innospots.nexus.core.plugin.declaration.CapabilityContribution;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.PluginSource;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 在运行时定义与持久化摘要之间执行显式、安全映射。 */
public final class PluginDefinitionSnapshotMapper {

    private PluginDefinitionSnapshotMapper() {
    }

    /**
     * 创建不含敏感配置值的定义快照。
     *
     * @param definition 运行时插件定义
     * @param source 声明来源元数据
     * @return 可持久化的安全定义快照
     * @throws NexusException 定义或来源为空时抛出
     */
    public static PluginDefinitionSnapshot from(PluginDefinition definition, PluginSource source) {
        return from(definition, source, PluginContributionSnapshotterRegistry.builder().build());
    }

    /**
     * 使用宿主注册的快照器生成 Contribution 摘要。
     *
     * @param definition 运行时插件定义
     * @param source 声明来源元数据
     * @param snapshotters 宿主注册的 Contribution 快照器表
     * @return 可持久化的安全定义快照
     * @throws NexusException 输入为空、缺少快照器或快照器返回 null 时抛出
     */
    public static PluginDefinitionSnapshot from(
            PluginDefinition definition,
            PluginSource source,
            PluginContributionSnapshotterRegistry snapshotters
    ) {
        if (definition == null || source == null || snapshotters == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_DEFINITION_INVALID,
                    "definition, source and snapshotters are required");
        }
        List<PluginDefinitionSnapshot.CapabilitySnapshot> capabilities = definition.capabilities().stream()
                .map(contribution -> capability(definition, contribution))
                .toList();
        List<Map<String, Object>> contributions = definition.contributions().stream()
                .map(contribution -> contributionSnapshot(contribution, snapshotters))
                .toList();
        return new PluginDefinitionSnapshot(
                definition.pluginId(), definition.version(), definition.apiVersion(),
                source.sourceType(), source.location(), capabilities, contributions);
    }

    /**
     * 序列化安全快照为 JSON。
     *
     * @param snapshot 待序列化的定义快照
     * @return JSON 文本
     * @throws NexusException 快照为空时抛出
     */
    public static String toJson(PluginDefinitionSnapshot snapshot) {
        if (snapshot == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "plugin definition snapshot is required");
        }
        return Jsons.toJson(snapshot);
    }

    /**
     * 从 JSON 恢复安全快照。
     *
     * @param json 定义快照 JSON 文本
     * @return 反序列化后的定义快照
     * @throws NexusException JSON 为空或结构非法时抛出
     */
    public static PluginDefinitionSnapshot fromJson(String json) {
        if (json == null || json.isBlank()) {
            throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "plugin definition snapshot JSON is required");
        }
        return Jsons.fromJson(json, PluginDefinitionSnapshot.class);
    }

    private static PluginDefinitionSnapshot.CapabilitySnapshot capability(
            PluginDefinition definition,
            CapabilityContribution<?> contribution
    ) {
        List<PluginDefinitionSnapshot.ConfigItemSnapshot> config = contribution.config().items().stream()
                .map(item -> new PluginDefinitionSnapshot.ConfigItemSnapshot(
                        item.key(), item.type().name(), item.required(), item.secret()))
                .toList();
        return new PluginDefinitionSnapshot.CapabilitySnapshot(
                contribution.type().key().name(), contribution.type().key().majorVersion(),
                contribution.providerId(),
                Tags.merge(definition.tags(), contribution.tags()).asMap(),
                config);
    }

    private static Map<String, Object> contributionSnapshot(
            PluginContribution contribution,
            PluginContributionSnapshotterRegistry snapshotters
    ) {
        PluginContributionSnapshotter<?> snapshotter = snapshotters.find(contribution.type()).orElse(null);
        if (snapshotter == null) {
            throw NexusException.build(PluginStatusCode.UNSUPPORTED_CONTRIBUTION_TYPE,
                    "missing contribution snapshotter: " + contribution.type());
        }
        return snapshot(snapshotter, contribution);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> snapshot(
            PluginContributionSnapshotter<?> snapshotter,
            PluginContribution contribution
    ) {
        Map<String, Object> result = ((PluginContributionSnapshotter<PluginContribution>) snapshotter)
                .snapshot(contribution);
        if (result == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "contribution snapshotter returned null: " + contribution.type());
        }
        return result;
    }
}

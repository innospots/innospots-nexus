package com.innospots.nexus.core.plugin.contribution.console;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.declaration.PluginDefinition;
import com.innospots.nexus.core.plugin.declaration.Tags;

/**
 * console@2 classpath entry 插件组装门面：{@link PluginDefinition} 与发现用标签。
 *
 * <p>贡献树节点请使用 {@link ConsoleContributeNode} 静态工厂；YAML 贡献由
 * {@link ConsolePluginContributionDecoder} 解码。运行时 SPI（Handler / Snapshotter）保持独立类型以便宿主注册。</p>
 */
public final class ConsoleContributions {

    public static final String DISCOVERY_TAG_KIND = "kind";
    public static final String DISCOVERY_TAG_KIND_ENTRY = "entry";
    public static final String DISCOVERY_TAG_DOMAIN = "domain";
    public static final String DISCOVERY_TAG_MODULE = "module";

    private ConsoleContributions() {
    }

    /**
     * 构建仅贡献 console@2 模块树的 entry 插件定义。
     */
    public static PluginDefinition entryPlugin(
            String pluginId,
            String version,
            Tags tags,
            I18nObject displayName,
            I18nObject description,
            String domainKey,
            List<ConsoleContributeNode> modules
    ) {
        return PluginDefinition.builder(pluginId)
                .version(version)
                .tags(tags)
                .displayName(displayName)
                .description(description)
                .contribute(new ConsolePluginContribution(
                        domainKey,
                        displayName,
                        description,
                        0,
                        List.copyOf(modules)))
                .build();
    }

    /**
     * entry 插件发现标签（kind / domain / module 键列表）。
     */
    public static Tags discoveryTags(
            String pluginId,
            String domainKey,
            List<String> moduleKeys
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(DISCOVERY_TAG_KIND, DISCOVERY_TAG_KIND_ENTRY);
        values.put(DISCOVERY_TAG_DOMAIN, domainKey);
        String moduleTag = moduleKeys.stream().sorted().collect(Collectors.joining(","));
        if (moduleTag.length() > 64) {
            moduleTag = pluginId.substring(pluginId.lastIndexOf('.') + 1);
        }
        values.put(DISCOVERY_TAG_MODULE, moduleTag);
        return Tags.from(values);
    }

    public static Tags discoveryTags(String pluginId, ConsolePluginContribution contribution) {
        List<String> moduleKeys = contribution.modules().stream()
                .map(ConsoleContributeNode::moduleKey)
                .sorted()
                .toList();
        return discoveryTags(pluginId, contribution.domainKey(), moduleKeys);
    }
}

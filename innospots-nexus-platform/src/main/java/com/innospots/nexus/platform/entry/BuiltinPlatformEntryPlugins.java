package com.innospots.nexus.platform.entry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributeNode;
import com.innospots.nexus.core.plugin.contribution.console.ConsoleContributions;
import com.innospots.nexus.core.plugin.declaration.Tags;

/**
 * {@code innospots-nexus-platform} 内置 entry 插件 ID、领域键、版本与发现标签。
 */
public final class BuiltinPlatformEntryPlugins {

    public static final String PLATFORM_CONSOLE = "com.innospots.nexus.platform.console";

    /** PageDsl / console 贡献领域：{@code ui-pages/platform/...}。 */
    public static final String DOMAIN_KEY = "platform";

    /** platform 宿主在通用 entry 标签之上追加的 realm 键。 */
    public static final String TAG_REALM = "realm";
    public static final String TAG_REALM_PLATFORM = "platform";

    public static final List<String> REQUIRED_PLUGIN_IDS = List.of(PLATFORM_CONSOLE);

    static final String DEVELOPMENT_PLUGIN_VERSION = "0.0.0-dev";

    private BuiltinPlatformEntryPlugins() {
    }

    public static String pluginVersion() {
        Package pkg = BuiltinPlatformEntryPlugins.class.getPackage();
        if (pkg == null) {
            return DEVELOPMENT_PLUGIN_VERSION;
        }
        String version = pkg.getImplementationVersion();
        if (version == null || version.isBlank()) {
            return DEVELOPMENT_PLUGIN_VERSION;
        }
        return version;
    }

    /**
     * platform 聚合 entry 的发现标签：{@link ConsoleContributions} 通用键 + {@link #TAG_REALM}。
     */
    public static Tags discoveryTags(List<ConsoleContributeNode> modules) {
        List<String> moduleKeys = modules.stream()
                .map(ConsoleContributeNode::moduleKey)
                .sorted()
                .toList();
        Tags base = ConsoleContributions.discoveryTags(PLATFORM_CONSOLE, DOMAIN_KEY, moduleKeys);
        Map<String, String> merged = new LinkedHashMap<>(base.asMap());
        merged.put(TAG_REALM, TAG_REALM_PLATFORM);
        return Tags.from(merged);
    }
}

package com.innospots.nexus.platform.entry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.innospots.nexus.core.plugin.capability.Tags;

/**
 * 运营管理平台内置 entry 插件身份、标签与宿主必选 ID。
 *
 * <p>单个聚合插件 {@link #PLATFORM_CONSOLE} 贡献 {@link #DOMAIN_KEY} 领域下全部 PageDsl 模块；
 * 版本取自 JAR {@code Implementation-Version}。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.entry.PlatformConsoleEntryPlugin
 * @see com.innospots.nexus.console.entry.BuiltinConsoleEntryPlugins
 */
public final class BuiltinPlatformEntryPlugins {

    /** 聚合 platform 管理能力的单 entry 插件 ID。 */
    public static final String PLATFORM_CONSOLE = "com.innospots.nexus.platform.console";

    /** PageDsl / console 贡献领域目录：{@code ui-pages/platform/...}。 */
    public static final String DOMAIN_KEY = "platform";

    public static final String TAG_KIND = "kind";
    public static final String TAG_KIND_ENTRY = "entry";
    public static final String TAG_DOMAIN = "domain";
    public static final String TAG_MODULE = "module";
    public static final String TAG_REALM = "realm";
    public static final String TAG_REALM_PLATFORM = "platform";

    static final String DEVELOPMENT_PLUGIN_VERSION = "0.0.0-dev";

    public static final java.util.List<String> REQUIRED_PLUGIN_IDS = java.util.List.of(PLATFORM_CONSOLE);

    private BuiltinPlatformEntryPlugins() {
    }

    /**
     * 返回 platform 构件版本，供 {@code PluginDefinition.version} 使用。
     *
     * @return 非空版本字符串；缺失 manifest 时返回开发态占位版本
     */
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
     * 为插件发现与路由生成 entry 标签（kind、realm、domain/module 键列表）。
     *
     * @param entry 聚合 entry 描述
     * @return 插件标签
     */
    public static Tags tagsFor(PlatformEntryPluginDescriptor entry) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(TAG_KIND, TAG_KIND_ENTRY);
        values.put(TAG_REALM, TAG_REALM_PLATFORM);
        values.put(TAG_DOMAIN, entry.modules().stream()
                .map(PlatformModuleDescriptors::domainKey)
                .distinct()
                .sorted()
                .collect(Collectors.joining(",")));
        values.put(TAG_MODULE, entry.modules().stream()
                .map(PlatformModuleDescriptors::moduleKey)
                .sorted()
                .collect(Collectors.joining(",")));
        return Tags.from(values);
    }
}

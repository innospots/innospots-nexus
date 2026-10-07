package com.innospots.nexus.console.entry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.innospots.nexus.core.plugin.capability.Tags;

/**
 * 内置控制台 entry 插件身份与组装元数据常量。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class BuiltinConsoleEntryPlugins {

    public static final String MENU = "com.innospots.nexus.console.menu";
    public static final String DICTIONARY = "com.innospots.nexus.console.dictionary";
    public static final String LOGGER = "com.innospots.nexus.console.logger";
    public static final String PERMISSION = "com.innospots.nexus.console.permission";
    public static final String ROLE = "com.innospots.nexus.console.role";
    public static final String PLUGIN_MANAGEMENT = "com.innospots.nexus.console.plugin-management";

    /** 插件标签：标识内置 entry 贡献（非 Capability 路由用途）。 */
    public static final String TAG_KIND = "kind";
    public static final String TAG_KIND_ENTRY = "entry";

    /** 插件标签：PageDsl / console 贡献领域键。 */
    public static final String TAG_DOMAIN = "domain";

    /** 插件标签：控制台模块键。 */
    public static final String TAG_MODULE = "module";

    /** 未打包 manifest 时的开发态回退版本（与 Maven ${revision} 解耦，仅本地 IDE 运行）。 */
    static final String DEVELOPMENT_PLUGIN_VERSION = "0.0.0-dev";

    public static final java.util.List<String> REQUIRED_PLUGIN_IDS = java.util.List.of(
            MENU,
            DICTIONARY,
            LOGGER,
            PERMISSION,
            ROLE,
            PLUGIN_MANAGEMENT);

    private BuiltinConsoleEntryPlugins() {
    }

    /**
     * 返回当前 {@code innospots-nexus-console} 构件版本，用于内置 entry 插件 {@code PluginDefinition.version}。
     *
     * <p>优先读取 JAR {@code Implementation-Version}（Maven {@code ${revision}}）；缺失时回退
     * {@link #DEVELOPMENT_PLUGIN_VERSION}。</p>
     *
     * @return 非空版本字符串
     */
    public static String pluginVersion() {
        Package pkg = BuiltinConsoleEntryPlugins.class.getPackage();
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
     * 根据模块描述符生成内置 entry 插件默认标签。
     *
     * @param descriptor 内置模块元数据
     * @return 不可变标签集合
     */
    public static Tags tagsFor(ConsoleModuleDescriptor descriptor) {
        return tagsFor(ConsoleEntryPluginDescriptor.of(descriptor));
    }

    /**
     * 根据 entry 描述符生成内置 entry 插件默认标签。
     *
     * @param entry entry 插件元数据
     * @return 不可变标签集合
     */
    public static Tags tagsFor(ConsoleEntryPluginDescriptor entry) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(TAG_KIND, TAG_KIND_ENTRY);
        values.put(TAG_DOMAIN, entry.modules().stream()
                .map(ConsoleModuleDescriptor::domainKey)
                .distinct()
                .sorted()
                .collect(Collectors.joining(",")));
        values.put(TAG_MODULE, entry.modules().stream()
                .map(ConsoleModuleDescriptor::moduleKey)
                .sorted()
                .collect(Collectors.joining(",")));
        return Tags.from(values);
    }
}

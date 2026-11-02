package com.innospots.nexus.platform.entry;

import java.util.List;

/**
 * {@code innospots-nexus-platform} 内置 entry 插件 ID、领域键、版本与发现标签常量。
 *
 * <p>控制台 MODULE 树声明见 classpath
 * {@code META-INF/innos-resources/plugin-declaration.yaml}。</p>
 */
public final class BuiltinPlatformEntryPlugins {

    public static final String PLATFORM_CONSOLE = "com.innospots.nexus.platform.console";

    /** platform console 贡献领域键。 */
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
}

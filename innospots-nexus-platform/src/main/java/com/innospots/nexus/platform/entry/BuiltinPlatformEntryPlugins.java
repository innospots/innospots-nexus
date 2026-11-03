package com.innospots.nexus.platform.entry;

import java.util.List;

/**
 * {@code innospots-nexus-platform} 内置 entry 插件 ID、领域键、版本与发现标签常量。
 *
 * <p>控制台 MODULE 树声明见 classpath
 * {@code META-INF/innos-resources/plugin-declaration.yaml}。
 * {@link #SYSTEM_DOMAIN_KEY} 须与
 * {@code com.innospots.nexus.console.entry.ConsolePageRoutes#SYSTEM_DOMAIN_KEY} 保持一致。</p>
 */
public final class BuiltinPlatformEntryPlugins {

    public static final String PLATFORM_CONSOLE = "com.innospots.nexus.platform.console";

    /** 系统管理 catalog 领域键（设置、主题、审计、字典；插件 MODULE 由 console entry 贡献）。 */
    public static final String SYSTEM_DOMAIN_KEY = "system";

    /** 运营管理 catalog 领域键（角色、用户、租户）。 */
    public static final String OPERATION_DOMAIN_KEY = "operation";

    /**
     * @deprecated 使用 {@link #SYSTEM_DOMAIN_KEY} 或 {@link #OPERATION_DOMAIN_KEY}。
     */
    @Deprecated
    public static final String DOMAIN_KEY = OPERATION_DOMAIN_KEY;

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

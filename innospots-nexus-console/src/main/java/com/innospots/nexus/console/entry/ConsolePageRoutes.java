package com.innospots.nexus.console.entry;

/**
 * 控制台页面导航路径与推荐入口键格式。
 */
public final class ConsolePageRoutes {

    /** 控制台基础设施 catalog 领域（catalog、i18n、根路径等）。 */
    public static final String BUILTIN_DOMAIN_KEY = "nexus";

    /**
     * 系统管理类 catalog 领域（设置、插件等 MODULE）。
     *
     * <p>与 platform {@code plugin-declaration.yaml} 中 {@code domainKey: system} 对齐；
     * 授权键格式 {@code system.{moduleKey}:{grantName}}。</p>
     */
    public static final String SYSTEM_DOMAIN_KEY = "system";

    public static final String PAGE_ROUTE_PREFIX = "/page";

    private ConsolePageRoutes() {
    }

    /**
     * 生成推荐格式的稳定入口键。
     *
     * @param domainKey 领域键
     * @param moduleKey 模块键
     * @param pageSuffix 页面后缀
     * @return {@code domainKey-moduleKey-pageSuffix}
     */
    public static String entryPageKey(String domainKey, String moduleKey, String pageSuffix) {
        return domainKey + "-" + moduleKey + "-" + pageSuffix;
    }

    public static String pagePath(String domainKey, String moduleKey, String entryPageKey) {
        return PAGE_ROUTE_PREFIX + "/" + domainKey + "/" + moduleKey + "/" + entryPageKey;
    }
}

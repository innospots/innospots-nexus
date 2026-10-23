package com.innospots.nexus.console.entry;

/**
 * 控制台页面导航路径与推荐入口键格式。
 */
public final class ConsolePageRoutes {

    /** 随 {@code innospots-nexus-console} 发布的内置 entry 沿用的领域键。 */
    public static final String BUILTIN_DOMAIN_KEY = "nexus";

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

package com.innospots.nexus.console.entry;

import com.innospots.nexus.console.ui.spec.PageDslPageRef;

/**
 * 控制台 Page 路由与 PageDsl 复合 pageKey（归属 console UI，非 plugin 契约）。
 */
public final class ConsolePageRoutes {

    /** 随 {@code innospots-nexus-console} 发布的内置 entry 沿用的 PageDsl 领域键。 */
    public static final String BUILTIN_DOMAIN_KEY = "nexus";

    public static final String PAGE_ROUTE_PREFIX = "/page";

    private ConsolePageRoutes() {
    }

    public static String compositePageKey(String domainKey, String moduleKey, String pageSuffix) {
        return PageDslPageRef.encode(domainKey, moduleKey, pageSuffix);
    }

    public static String pagePath(String domainKey, String moduleKey, String pageKey) {
        return PAGE_ROUTE_PREFIX + "/" + domainKey + "/" + moduleKey + "/" + pageKey;
    }
}

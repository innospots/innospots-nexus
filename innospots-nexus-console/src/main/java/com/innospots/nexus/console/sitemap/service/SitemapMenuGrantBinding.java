package com.innospots.nexus.console.sitemap.service;

import java.util.List;
import java.util.Set;

import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

/**
 * 加载 meta-sitemap 后按 catalog 授权目录规范化菜单节点的模块绑定。
 */
final class SitemapMenuGrantBinding {

    private SitemapMenuGrantBinding() {
    }

    static void apply(SitemapConfig config, ConsoleCatalogService catalogService) {
        if (config == null || catalogService == null) {
            return;
        }
        Set<String> grantKeys = catalogService.grantResourceKeys();
        applyMenus(config.getMenus(), grantKeys);
    }

    private static void applyMenus(List<SitemapMenuItem> menus, Set<String> grantKeys) {
        if (menus == null || menus.isEmpty()) {
            return;
        }
        for (SitemapMenuItem menu : menus) {
            applyNode(menu, grantKeys);
        }
    }

    private static void applyNode(SitemapMenuItem menu, Set<String> grantKeys) {
        if (menu == null) {
            return;
        }
        if (menu.hasCatalogGrantBinding() && !hasText(menu.getViewGroup())) {
            menu.setViewGroup("default");
        }
        String viewGrantKey = menu.viewGrantKey();
        if (viewGrantKey != null && !grantKeys.contains(viewGrantKey)) {
            clearCatalogBinding(menu);
        }
        applyMenus(menu.getChildren(), grantKeys);
    }

    private static void clearCatalogBinding(SitemapMenuItem menu) {
        menu.setDomainKey(null);
        menu.setModuleKey(null);
        menu.setViewGroup(null);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

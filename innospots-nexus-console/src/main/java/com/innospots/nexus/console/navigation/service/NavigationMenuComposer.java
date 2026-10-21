package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.sitemap.service.SitemapService;

/**
 * 组合 catalog 导航与 meta-sitemap 默认菜单（后者经角色裁剪后转换）。
 */
public final class NavigationMenuComposer {

    private final NavigationMenuAssembler catalogAssembler;
    private final SitemapService sitemapService;
    private final MetaSitemapMenuFilter metaSitemapMenuFilter;
    private final MetaSitemapNavigationConverter metaSitemapNavigationConverter;

    public NavigationMenuComposer(
            NavigationMenuAssembler catalogAssembler,
            SitemapService sitemapService,
            MetaSitemapMenuFilter metaSitemapMenuFilter,
            MetaSitemapNavigationConverter metaSitemapNavigationConverter
    ) {
        if (catalogAssembler == null || sitemapService == null || metaSitemapMenuFilter == null
                || metaSitemapNavigationConverter == null) {
            throw new IllegalArgumentException("NavigationMenuComposer dependencies are required");
        }
        this.catalogAssembler = catalogAssembler;
        this.sitemapService = sitemapService;
        this.metaSitemapMenuFilter = metaSitemapMenuFilter;
        this.metaSitemapNavigationConverter = metaSitemapNavigationConverter;
    }

    /**
     * 插件 catalog 可见菜单 + meta-sitemap 默认项（catalog 优先）。
     */
    public List<NavigationMenuVo> compose(ConsoleOwnership ownership, AuthorizationSubject subject) {
        List<NavigationMenuVo> catalogMenus = catalogAssembler.navigationMenus(ownership, subject);
        List<NavigationMenuVo> configMenus = metaSitemapNavigationConverter.toNavigationMenus(
                metaSitemapMenuFilter.filterVisibleMenus(sitemapService.load().getMenus(), subject));
        if (catalogMenus.isEmpty()) {
            return configMenus;
        }
        if (configMenus.isEmpty()) {
            return catalogMenus;
        }
        List<NavigationMenuVo> merged = new ArrayList<>(catalogMenus.size() + configMenus.size());
        merged.addAll(catalogMenus);
        merged.addAll(configMenus);
        return List.copyOf(merged);
    }
}

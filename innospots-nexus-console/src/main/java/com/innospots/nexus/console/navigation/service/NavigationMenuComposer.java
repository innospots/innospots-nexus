package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;
import com.innospots.nexus.console.sitemap.service.SitemapService;

/**
 * 组合持久化菜单与 meta-sitemap 默认菜单。
 */
public final class NavigationMenuComposer {

    private final NavigationMenuAssembler menuAssembler;
    private final SitemapService sitemapService;
    private final MetaSitemapMenuFilter metaSitemapMenuFilter;
    private final MetaSitemapNavigationConverter metaSitemapNavigationConverter;
    private final ConsoleWebSecuritySettings securitySettings;

    public NavigationMenuComposer(
            NavigationMenuAssembler menuAssembler,
            SitemapService sitemapService,
            MetaSitemapMenuFilter metaSitemapMenuFilter,
            MetaSitemapNavigationConverter metaSitemapNavigationConverter
    ) {
        this(
                menuAssembler,
                sitemapService,
                metaSitemapMenuFilter,
                metaSitemapNavigationConverter,
                new ConsoleWebSecuritySettings());
    }

    public NavigationMenuComposer(
            NavigationMenuAssembler menuAssembler,
            SitemapService sitemapService,
            MetaSitemapMenuFilter metaSitemapMenuFilter,
            MetaSitemapNavigationConverter metaSitemapNavigationConverter,
            ConsoleWebSecuritySettings securitySettings
    ) {
        if (menuAssembler == null || sitemapService == null || metaSitemapMenuFilter == null
                || metaSitemapNavigationConverter == null || securitySettings == null) {
            throw new IllegalArgumentException("NavigationMenuComposer dependencies are required");
        }
        this.menuAssembler = menuAssembler;
        this.sitemapService = sitemapService;
        this.metaSitemapMenuFilter = metaSitemapMenuFilter;
        this.metaSitemapNavigationConverter = metaSitemapNavigationConverter;
        this.securitySettings = securitySettings;
    }

    /**
     * 持久化菜单 + meta-sitemap 默认项（持久化菜单优先）。
     *
     * <p>请求侧安全关闭时返回全部启用菜单，不执行权限或角色过滤；安全开启时分别按
     * 菜单授权与 meta-sitemap {@code roles} 裁剪。</p>
     */
    public List<NavigationMenuVo> compose(ConsoleOwnership ownership, AuthorizationSubject subject) {
        boolean securityEnabled = securitySettings.isEnabled();
        List<NavigationMenuVo> persistedMenus = securityEnabled
                ? menuAssembler.navigationMenus(ownership, subject)
                : menuAssembler.allNavigationMenus(ownership);
        List<SitemapMenuItem> configuredMenus = sitemapService.load().getMenus();
        List<NavigationMenuVo> configMenus = metaSitemapNavigationConverter.toNavigationMenus(
                securityEnabled
                        ? metaSitemapMenuFilter.filterVisibleMenus(configuredMenus, subject)
                        : configuredMenus);
        if (persistedMenus.isEmpty()) {
            return configMenus;
        }
        if (configMenus.isEmpty()) {
            return persistedMenus;
        }
        List<NavigationMenuVo> merged = new ArrayList<>(persistedMenus.size() + configMenus.size());
        merged.addAll(persistedMenus);
        merged.addAll(configMenus);
        return List.copyOf(merged);
    }
}

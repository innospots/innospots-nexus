package com.innospots.nexus.console.navigation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings;
import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;
import com.innospots.nexus.console.sitemap.service.SitemapService;

class NavigationMenuComposerTest {

    @Test
    void disabledSecurityStillOmitsHiddenMetaSitemapMenus() {
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT");
        AuthorizationSubject subject = new AuthorizationSubject("u1", Set.of(), Set.of(), false);
        NavigationMenuAssembler assembler = mock(NavigationMenuAssembler.class);
        when(assembler.allNavigationMenus(ownership)).thenReturn(List.of());

        SitemapService sitemapService = mock(SitemapService.class);
        SitemapConfig sitemap = new SitemapConfig();
        SitemapMenuItem visible = sitemapMenu();
        visible.setId("configured");
        visible.setRoles(List.of());
        SitemapMenuItem hidden = sitemapMenu();
        hidden.setId("hidden-page");
        hidden.setHidden(true);
        sitemap.setMenus(List.of(visible, hidden));
        when(sitemapService.load()).thenReturn(sitemap);

        NavigationMenuComposer composer = new NavigationMenuComposer(
                assembler,
                sitemapService,
                metaFilter(ownership, subject, Set.of()),
                new MetaSitemapNavigationConverter(),
                disabledSecurity());

        assertThat(composer.compose(ownership, subject))
                .extracting(NavigationMenuVo::menuKey)
                .containsExactly("configured");
    }

    private static ConsoleWebSecuritySettings disabledSecurity() {
        ConsoleWebSecuritySettings security = new ConsoleWebSecuritySettings();
        security.setEnabled(false);
        return security;
    }

    @Test
    void disabledSecurityReturnsAllMenuSourcesWithoutRoleFiltering() {
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT");
        AuthorizationSubject subject = new AuthorizationSubject("u1", Set.of(), Set.of(), false);
        NavigationMenuAssembler assembler = mock(NavigationMenuAssembler.class);
        List<NavigationMenuVo> persistedMenus = List.of(navigationMenu("persisted"));
        when(assembler.allNavigationMenus(ownership)).thenReturn(persistedMenus);

        SitemapService sitemapService = mock(SitemapService.class);
        SitemapConfig sitemap = new SitemapConfig();
        sitemap.setMenus(List.of(sitemapMenu()));
        when(sitemapService.load()).thenReturn(sitemap);

        ConsoleWebSecuritySettings security = disabledSecurity();
        NavigationMenuComposer composer = new NavigationMenuComposer(
                assembler,
                sitemapService,
                metaFilter(ownership, subject, Set.of()),
                new MetaSitemapNavigationConverter(),
                security);

        List<NavigationMenuVo> menus = composer.compose(ownership, subject);

        assertThat(menus).extracting(NavigationMenuVo::menuKey)
                .containsExactly("persisted", "restricted");
    }

    @Test
    void enabledSecurityUsesPermissionAndRoleFiltering() {
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT");
        AuthorizationSubject subject = new AuthorizationSubject("u1", Set.of(), Set.of(), false);
        NavigationMenuAssembler assembler = mock(NavigationMenuAssembler.class);
        when(assembler.navigationMenus(ownership, subject)).thenReturn(List.of());

        SitemapService sitemapService = mock(SitemapService.class);
        SitemapConfig sitemap = new SitemapConfig();
        sitemap.setMenus(List.of(sitemapMenu()));
        when(sitemapService.load()).thenReturn(sitemap);

        NavigationMenuComposer composer = new NavigationMenuComposer(
                assembler,
                sitemapService,
                metaFilter(ownership, subject, Set.of()),
                new MetaSitemapNavigationConverter(),
                new ConsoleWebSecuritySettings());

        assertThat(composer.compose(ownership, subject)).isEmpty();
    }

    @Test
    void emptyPersistedMenusReturnConfiguredMenusWithoutMerging() {
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership("ws-1", "TENANT");
        AuthorizationSubject subject = new AuthorizationSubject("u1", Set.of(), Set.of(), false);
        NavigationMenuAssembler assembler = mock(NavigationMenuAssembler.class);
        when(assembler.navigationMenus(ownership, subject)).thenReturn(List.of());

        SitemapService sitemapService = mock(SitemapService.class);
        SitemapConfig sitemap = new SitemapConfig();
        SitemapMenuItem unrestricted = sitemapMenu();
        unrestricted.setId("configured");
        unrestricted.setRoles(List.of());
        sitemap.setMenus(List.of(unrestricted));
        when(sitemapService.load()).thenReturn(sitemap);

        NavigationMenuComposer composer = new NavigationMenuComposer(
                assembler,
                sitemapService,
                metaFilter(ownership, subject, Set.of()),
                new MetaSitemapNavigationConverter(),
                new ConsoleWebSecuritySettings());

        assertThat(composer.compose(ownership, subject))
                .extracting(NavigationMenuVo::menuKey)
                .containsExactly("configured");
    }

    private static MetaSitemapMenuFilter metaFilter(
            ConsoleOwnership ownership,
            AuthorizationSubject subject,
            Set<String> grantedKeys
    ) {
        PermissionVisibilityService visibilityService = mock(PermissionVisibilityService.class);
        when(visibilityService.grantedGrantKeys(ownership, subject)).thenReturn(grantedKeys);
        return new MetaSitemapMenuFilter(visibilityService);
    }

    private static SitemapMenuItem sitemapMenu() {
        SitemapMenuItem menu = new SitemapMenuItem();
        menu.setId("restricted");
        menu.setType("page");
        menu.setPageId("restricted-page");
        menu.setTitle("Restricted");
        menu.setRoles(List.of("role-a"));
        return menu;
    }

    private static NavigationMenuVo navigationMenu(String key) {
        return new NavigationMenuVo(
                key,
                key,
                null,
                null,
                null,
                null,
                MenuOpenMode.INTERNAL,
                null,
                null,
                null,
                null,
                List.of());
    }
}

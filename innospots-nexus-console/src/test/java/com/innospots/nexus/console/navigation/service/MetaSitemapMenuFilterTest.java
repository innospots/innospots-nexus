package com.innospots.nexus.console.navigation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

class MetaSitemapMenuFilterTest {

    private final ConsoleOwnership ownership = new ConsoleOwnership(
            RoleOwnerType.PLATFORM, null, SecurityRealm.PLATFORM.name());

    @Test
    void withoutHiddenMenusRemovesHiddenNodes() {
        MetaSitemapMenuFilter filter = new MetaSitemapMenuFilter(mock(PermissionVisibilityService.class));
        SitemapMenuItem visible = menu("visible", false);
        SitemapMenuItem hidden = menu("hidden", true);

        List<SitemapMenuItem> result = filter.withoutHiddenMenus(List.of(visible, hidden));

        assertThat(result).extracting(SitemapMenuItem::getId).containsExactly("visible");
    }

    @Test
    void filtersByRolePresetWhenNoCatalogBinding() {
        PermissionVisibilityService visibilityService = mock(PermissionVisibilityService.class);
        when(visibilityService.grantedGrantKeys(ownership, subject("role-a")))
                .thenReturn(Set.of());
        MetaSitemapMenuFilter filter = new MetaSitemapMenuFilter(visibilityService);

        SitemapMenuItem allowed = menu("allowed", List.of("role-a"), null, null, null);
        SitemapMenuItem denied = menu("denied", List.of("role-b"), null, null, null);
        SitemapMenuItem unrestricted = menu("unrestricted", List.of(), null, null, null);
        AuthorizationSubject subject = subject("role-a");

        List<SitemapMenuItem> visible = filter.filterVisibleMenus(
                List.of(allowed, denied, unrestricted), ownership, subject);

        assertThat(visible).extracting(SitemapMenuItem::getId)
                .containsExactly("allowed", "unrestricted");
    }

    @Test
    void filtersByCatalogViewGrantKey() {
        PermissionVisibilityService visibilityService = mock(PermissionVisibilityService.class);
        AuthorizationSubject subject = subject("role-a");
        when(visibilityService.grantedGrantKeys(ownership, subject))
                .thenReturn(Set.of("platform.user:default"));
        MetaSitemapMenuFilter filter = new MetaSitemapMenuFilter(visibilityService);

        SitemapMenuItem granted = menu("users", List.of(), "platform", "user", "default");
        SitemapMenuItem denied = menu("roles", List.of(), "platform", "role", "default");

        List<SitemapMenuItem> visible = filter.filterVisibleMenus(
                List.of(granted, denied), ownership, subject);

        assertThat(visible).extracting(SitemapMenuItem::getId).containsExactly("users");
    }

    private static AuthorizationSubject subject(String roleId) {
        return new AuthorizationSubject("u1", Set.of(roleId), Set.of(), false);
    }

    private static SitemapMenuItem menu(String id, boolean hidden) {
        SitemapMenuItem item = new SitemapMenuItem();
        item.setId(id);
        item.setType("page");
        item.setTitle(id);
        item.setHidden(hidden);
        return item;
    }

    private static SitemapMenuItem menu(
            String id,
            List<String> roles,
            String domainKey,
            String moduleKey,
            String viewGroup
    ) {
        SitemapMenuItem item = new SitemapMenuItem();
        item.setId(id);
        item.setType("page");
        item.setTitle(id);
        item.setRoles(roles);
        item.setDomainKey(domainKey);
        item.setModuleKey(moduleKey);
        item.setViewGroup(viewGroup);
        return item;
    }
}

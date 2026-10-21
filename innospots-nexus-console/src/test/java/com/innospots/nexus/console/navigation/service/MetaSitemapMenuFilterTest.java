package com.innospots.nexus.console.navigation.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

class MetaSitemapMenuFilterTest {

    private final MetaSitemapMenuFilter filter = new MetaSitemapMenuFilter();

    @Test
    void filtersByRolePreset() {
        SitemapMenuItem allowed = menu("allowed", List.of("role-a"));
        SitemapMenuItem denied = menu("denied", List.of("role-b"));
        AuthorizationSubject subject = new AuthorizationSubject("u1", Set.of("role-a"), Set.of(), false);

        List<SitemapMenuItem> visible = filter.filterVisibleMenus(List.of(allowed, denied), subject);

        assertThat(visible).extracting(SitemapMenuItem::getId).containsExactly("allowed");
    }

    private static SitemapMenuItem menu(String id, List<String> roles) {
        SitemapMenuItem item = new SitemapMenuItem();
        item.setId(id);
        item.setType("page");
        item.setTitle(id);
        item.setRoles(roles);
        return item;
    }
}

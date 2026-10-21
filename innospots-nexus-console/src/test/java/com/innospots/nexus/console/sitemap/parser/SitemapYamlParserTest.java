package com.innospots.nexus.console.sitemap.parser;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SitemapYamlParserTest {

    private final SitemapYamlParser parser = new SitemapYamlParser();

    @Test
    void parsesSiteMetadataAndMenuGroupTree() {
        String yaml = """
                id: platform-console
                title: Platform
                description: desc
                version: 2
                updatedAt: "2026-10-21T00:00:00Z"
                menus:
                  - id: operations
                    type: group
                    title: 运营管理
                    icon: users
                    order: 2
                    children:
                      - { id: nav-tenants, type: page, pageId: tenant-list, title: 租户, layout: admin }
                  - { id: hidden-role, type: page, pageId: role-detail, layout: admin, hidden: true, activeMenu: nav-roles }
                """;
        SitemapConfig config = parser.parse(yaml);

        assertThat(config.getId()).isEqualTo("platform-console");
        assertThat(config.getTitle()).isEqualTo("Platform");
        assertThat(config.getDescription()).isEqualTo("desc");
        assertThat(config.getVersion()).isEqualTo(2);
        assertThat(config.getUpdatedAt()).isEqualTo("2026-10-21T00:00:00Z");
        assertThat(config.getMenus()).hasSize(2);

        SitemapMenuItem operations = config.getMenus().getFirst();
        assertThat(operations.getType()).isEqualTo("group");
        assertThat(operations.getChildren()).hasSize(1);
        assertThat(operations.getChildren().getFirst().getPageId()).isEqualTo("tenant-list");

        SitemapMenuItem hidden = config.getMenus().get(1);
        assertThat(hidden.getHidden()).isTrue();
        assertThat(hidden.getActiveMenu()).isEqualTo("nav-roles");
    }

    @Test
    void parsesRolesOnMenuItem() {
        String yaml = """
                id: site
                menus:
                  - id: admin-only
                    type: page
                    pageId: settings
                    roles:
                      - platform-admin
                      - ops-lead
                """;
        SitemapConfig config = parser.parse(yaml);

        assertThat(config.getMenus().getFirst().getRoles())
                .containsExactly("platform-admin", "ops-lead");
    }

    @Test
    void ignoresUnknownProperties() {
        String yaml = """
                id: site
                unknownRoot: true
                menus:
                  - id: x
                    type: page
                    pageId: x
                    extraField: ignored
                """;
        SitemapConfig config = parser.parse(yaml);

        assertThat(config.getMenus()).hasSize(1);
        assertThat(config.getMenus().getFirst().getPageId()).isEqualTo("x");
    }

    @Test
    void rejectsBlankContent() {
        assertThatThrownBy(() -> parser.parse("  "))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }

    @Test
    void rejectsInvalidYaml() {
        assertThatThrownBy(() -> parser.parse("menus:\n  - id: [unclosed"))
                .isInstanceOf(NexusException.class)
                .hasMessageContaining("Cannot parse sitemap YAML");
    }
}

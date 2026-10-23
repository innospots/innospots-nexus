package com.innospots.nexus.console.sitemap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;
import com.innospots.nexus.console.sitemap.service.SitemapService;

class SitemapYamlLoadTest {

    @Test
    void loadsMetaSitemapYaml() {
        SitemapConfigLoader loader = new SitemapConfigLoader(
                SitemapYamlConfig.metaSitemapDefaults(),
                new SitemapYamlParser(),
                SitemapYamlLoadTest.class.getClassLoader());
        SitemapConfig config = loader.load();

        assertThat(config.getId()).isEqualTo("test-site");
        assertThat(config.getTitle()).isEqualTo("Test Console");
        assertThat(config.getMenus()).hasSize(1);
        assertThat(config.getMenus().getFirst().getPageId()).isEqualTo("demo-main");
    }

    @Test
    void parsesMenuItemAlignedWithFrontendSitemapYaml() {
        String yaml = """
                id: site
                title: Site
                menus:
                  - { id: login, type: page, pageId: login, title: 登录, layout: login, public: true, hidden: true }
                """;
        SitemapConfig config = new SitemapYamlParser().parse(yaml);
        SitemapMenuItem login = config.getMenus().getFirst();

        assertThat(login.getId()).isEqualTo("login");
        assertThat(login.getType()).isEqualTo("page");
        assertThat(login.getPageId()).isEqualTo("login");
        assertThat(login.getTitle()).isEqualTo("登录");
        assertThat(login.getLayout()).isEqualTo("login");
        assertThat(login.getPublicAccess()).isTrue();
        assertThat(login.getHidden()).isTrue();
    }

    @Test
    void loadsGroupFixtureFromClasspath() {
        SitemapConfig config = new SitemapConfigLoader(
                SitemapYamlConfig.ofResourcePath("console/group-meta-sitemap.yaml"),
                new SitemapYamlParser(),
                SitemapYamlLoadTest.class.getClassLoader()).load();

        assertThat(config.getMenus().getFirst().getType()).isEqualTo("group");
        assertThat(config.getMenus().getFirst().getChildren().getFirst().getPageId())
                .isEqualTo("role-list");
    }

    @Test
    void loadsThroughService() {
        SitemapService service = new SitemapService(
                new SitemapConfigLoader(
                        SitemapYamlConfig.metaSitemapDefaults(),
                        new SitemapYamlParser(),
                        SitemapYamlLoadTest.class.getClassLoader()));
        SitemapConfig resource = service.load();

        assertThat(resource.getMenus().getFirst().getLayout()).isEqualTo("admin");
    }
}

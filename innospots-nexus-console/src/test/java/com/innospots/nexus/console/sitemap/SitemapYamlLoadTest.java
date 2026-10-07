package com.innospots.nexus.console.sitemap;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;
import com.innospots.nexus.console.sitemap.service.SitemapMapper;
import com.innospots.nexus.console.sitemap.service.SitemapService;

class SitemapYamlLoadTest {

    @Test
    void loadsNexusSitemapYaml() {
        SitemapConfigLoader loader = new SitemapConfigLoader(
                SitemapYamlConfig.defaults(),
                new SitemapYamlParser(),
                SitemapYamlLoadTest.class.getClassLoader());
        SitemapConfig config = loader.load("nexus");

        assertThat(config.getId()).isEqualTo("site");
        assertThat(config.getResourceType()).isEqualTo("sitemap");
        assertThat(config.getPages()).isNotEmpty();
        assertThat(config.getMenus()).isNotEmpty();
        assertThat(config.getLayouts()).containsKeys("admin", "login", "blank");
        assertThat(config.getLayouts().get("admin").getBody().getType()).isEqualTo("Layout");
        assertThat(config.getLayouts().get("admin").getBody().getProps()).isNotEmpty();
    }

    @Test
    void rendersFixedYamlThroughService() {
        SitemapService service = new SitemapService(
                new SitemapConfigLoader(
                        SitemapYamlConfig.defaults(),
                        new SitemapYamlParser(),
                        SitemapYamlLoadTest.class.getClassLoader()),
                new SitemapMapper());
        SitemapResource resource = service.render("nexus");

        assertThat(resource.getId()).isEqualTo("site");
        assertThat(resource.getPages()).hasSize(6);
        assertThat(resource.getMenus()).hasSize(6);
        assertThat(resource.getPages().getFirst().getId()).isEqualTo("nexus-menu-main");
        assertThat(resource.getPages().getFirst().getPath()).isEqualTo("/page/nexus/menu/nexus-menu-main");
        assertThat(resource.getLayouts().get("login").getBody().getType()).isEqualTo("Flex");
    }
}

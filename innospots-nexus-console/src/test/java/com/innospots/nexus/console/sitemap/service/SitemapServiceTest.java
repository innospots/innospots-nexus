package com.innospots.nexus.console.sitemap.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SitemapServiceTest {

    @Test
    void requiresConfigLoader() {
        assertThatThrownBy(() -> new SitemapService(null))
                .isInstanceOf(NexusException.class);
    }

    @Test
    void delegatesLoadToConfigLoader() {
        SitemapService service = new SitemapService(
                new SitemapConfigLoader(
                        SitemapYamlConfig.metaSitemapDefaults(),
                        new SitemapYamlParser(),
                        SitemapServiceTest.class.getClassLoader()));
        SitemapConfig config = service.load();

        assertThat(config.getId()).isEqualTo("test-site");
        assertThat(config.getMenus()).isNotEmpty();
    }
}

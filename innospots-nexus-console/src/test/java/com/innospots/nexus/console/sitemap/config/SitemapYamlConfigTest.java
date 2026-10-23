package com.innospots.nexus.console.sitemap.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SitemapYamlConfigTest {

    @Test
    void metaSitemapDefaultsPointsToConsoleMetaSitemapYaml() {
        assertThat(SitemapYamlConfig.metaSitemapDefaults().resourcePath())
                .isEqualTo("console/meta-sitemap.yaml");
    }

    @Test
    void rejectsBlankResourcePath() {
        assertThatThrownBy(() -> SitemapYamlConfig.ofResourcePath("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("resourcePath");
    }
}

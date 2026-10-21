package com.innospots.nexus.console.sitemap.loader;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SitemapConfigLoaderTest {

    @Test
    void failsWhenResourceMissing() {
        SitemapConfigLoader loader = new SitemapConfigLoader(
                SitemapYamlConfig.ofResourcePath("ui-pages/missing-meta-sitemap.yaml"),
                new SitemapYamlParser(),
                SitemapConfigLoaderTest.class.getClassLoader());

        assertThatThrownBy(loader::load)
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(NexusStatusCode.CONFIG_ERROR.fullCode());
    }

    @Test
    void rejectsNullConfigOrParser() {
        assertThatThrownBy(() -> new SitemapConfigLoader(null, new SitemapYamlParser(), getClass().getClassLoader()))
                .isInstanceOf(NexusException.class);
        assertThatThrownBy(() -> new SitemapConfigLoader(
                SitemapYamlConfig.metaSitemapDefaults(), null, getClass().getClassLoader()))
                .isInstanceOf(NexusException.class);
    }
}

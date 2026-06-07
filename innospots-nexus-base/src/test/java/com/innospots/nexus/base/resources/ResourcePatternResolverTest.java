package com.innospots.nexus.base.resources;

import cn.hutool.core.io.resource.Resource;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourcePatternResolverTest {

    private final ResourcePatternResolver resolver = new ResourcePatternResolver();

    // ---- Exact path resolution ----

    @Test
    void resolvesExactClasspathResource() throws IOException {
        List<Resource> resources = resolver.getResources("classpath:config/application.properties");
        assertThat(resources).hasSize(1);
        Resource r = resources.get(0);
        assertThat(r.getName()).isEqualTo("application.properties");
        assertThat(r.getUrl()).isNotNull();
        assertThat(r.readUtf8Str()).contains("app.name");
    }

    @Test
    void resolvesExactClasspathAllResource() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:config/application.properties");
        assertThat(resources).hasSize(1);
        Resource r = resources.get(0);
        assertThat(r.readUtf8Str()).contains("app.version");
    }

    @Test
    void resolvesExactResourceWithoutPrefix() throws IOException {
        List<Resource> resources = resolver.getResources("config/application.properties");
        assertThat(resources).isNotEmpty();
    }

    @Test
    void returnsEmptyForNonExistentExactPath() throws IOException {
        List<Resource> resources = resolver.getResources("classpath:nonexistent/file.txt");
        assertThat(resources).isEmpty();
    }

    // ---- Pattern matching ----

    @Test
    void resolvesAllXmlFilesUnderMapper() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:mapper/**/*.xml");
        assertThat(resources).hasSize(2);
        List<String> names = resources.stream().map(Resource::getName).sorted().toList();
        assertThat(names).containsExactly("OrderMapper.xml", "UserMapper.xml");
    }

    @Test
    void resolvesByFileExtension() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:**/*.properties");
        assertThat(resources).isNotEmpty();
        assertThat(resources).allMatch(r -> r.getName().endsWith(".properties"));
    }

    @Test
    void resolvesByFileNamePattern() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:**/application.*");
        assertThat(resources).isNotEmpty();
        assertThat(resources).allMatch(r -> r.getName().startsWith("application"));
    }

    @Test
    void resolvesWithSingleAsterisk() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:config/*.properties");
        assertThat(resources).hasSize(1);
    }

    @Test
    void returnsEmptyForNonMatchingPattern() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:**/*.unknown");
        assertThat(resources).isEmpty();
    }

    // ---- Classpath*: vs classpath: ----

    @Test
    void classpathPrefixScanSingleRoot() throws IOException {
        List<Resource> single = resolver.getResources("classpath:mapper/**/*.xml");
        List<Resource> all = resolver.getResources("classpath*:mapper/**/*.xml");
        assertThat(single).isNotEmpty();
        assertThat(all).hasSameSizeAs(single);
    }

    // ---- MatchedResource specific ----

    @Test
    void matchedResourceRetainsPath() throws IOException {
        List<ResourcePatternResolver.MatchedResource> matched =
                resolver.getMatchedResources("classpath*:mapper/**/*.xml");
        assertThat(matched).isNotEmpty();
        for (ResourcePatternResolver.MatchedResource m : matched) {
            assertThat(m.getPath()).startsWith("mapper/");
            assertThat(m.getPath()).endsWith(".xml");
            assertThat(m.getName()).endsWith(".xml");
            assertThat(m.getUrl()).isNotNull();
            assertThat(m.getStream()).isNotNull();
            assertThat(m.getResource()).isNotNull();
        }
    }

    @Test
    void matchedResourceContentIsReadable() throws IOException {
        List<ResourcePatternResolver.MatchedResource> matched =
                resolver.getMatchedResources("classpath*:config/application.properties");
        assertThat(matched).hasSize(1);
        ResourcePatternResolver.MatchedResource m = matched.get(0);
        assertThat(m.getPath()).isEqualTo("config/application.properties");
        String content = m.readUtf8Str();
        assertThat(content).contains("app.name=innospots-nexus");
    }

    @Test
    void getResourcesReturnsSameCountAsMatched() throws IOException {
        List<Resource> plain = resolver.getResources("classpath*:mapper/**/*.xml");
        List<ResourcePatternResolver.MatchedResource> matched =
                resolver.getMatchedResources("classpath*:mapper/**/*.xml");
        assertThat(plain).hasSameSizeAs(matched);
    }

    // ---- InputStream ----

    @Test
    void resourceStreamIsReadable() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:data/sample.json");
        assertThat(resources).hasSize(1);
        try (InputStream in = resources.get(0).getStream()) {
            byte[] buf = new byte[1024];
            int len = in.read(buf);
            String content = new String(buf, 0, len);
            assertThat(content).contains("\"name\"");
        }
    }

    // ---- Deduplication ----

    @Test
    void deduplicatesResourcesByUrl() throws IOException {
        List<ResourcePatternResolver.MatchedResource> matched =
                resolver.getMatchedResources("classpath*:config/application.properties");
        assertThat(matched).hasSize(1);
    }

    // ---- Edge cases ----

    @Test
    void rejectsBlankLocationPattern() {
        assertThatThrownBy(() -> resolver.getResources(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> resolver.getResources(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructsWithCustomClassLoader() {
        ResourcePatternResolver custom = new ResourcePatternResolver(
                Thread.currentThread().getContextClassLoader()
        );
        assertThat(custom).isNotNull();
    }

    @Test
    void constructsWithNullClassLoaderFallsBackToDefault() {
        ResourcePatternResolver custom = new ResourcePatternResolver(null);
        assertThat(custom).isNotNull();
    }

    @Test
    void resolvesFilesInRootPackage() throws IOException {
        List<Resource> resources = resolver.getResources("classpath*:mapper/**/*.xml");
        assertThat(resources).isNotEmpty();
    }
}
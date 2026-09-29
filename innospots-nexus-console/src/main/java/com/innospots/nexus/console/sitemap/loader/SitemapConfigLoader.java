package com.innospots.nexus.console.sitemap.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;

/**
 * 从 classpath {@code ui-pages/{domainKey}/sitemap.yaml} 加载 sitemap 配置。
 */
public final class SitemapConfigLoader {

    private final SitemapYamlConfig config;
    private final SitemapYamlParser parser;
    private final ClassLoader classLoader;

    public SitemapConfigLoader(
            SitemapYamlConfig config,
            SitemapYamlParser parser,
            ClassLoader classLoader
    ) {
        if (config == null || parser == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Sitemap config and parser are required");
        }
        this.config = config;
        this.parser = parser;
        ClassLoader actual = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        if (actual == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Sitemap classLoader is required");
        }
        this.classLoader = actual;
    }

    /**
     * 加载指定领域的 sitemap 配置。
     *
     * @param domainKey {@code ui-pages} 首段目录名
     * @return 解析后的配置文档
     */
    public SitemapConfig load(String domainKey) {
        String resourcePath = config.resourcePath(domainKey);
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Sitemap resource not found: " + resourcePath);
            }
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            SitemapConfig document = parser.parse(content);
            if (document.getResourceType() != null && !"sitemap".equals(document.getResourceType())) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Sitemap resourceType must be sitemap: " + resourcePath);
            }
            return document;
        } catch (IOException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read sitemap resource: " + resourcePath,
                    exception);
        }
    }
}

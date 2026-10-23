package com.innospots.nexus.console.sitemap.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.config.SitemapYamlConfig;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.parser.SitemapYamlParser;

/**
 * 从 classpath {@code console/meta-sitemap.yaml} 加载默认菜单配置。
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
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Meta-sitemap config and parser are required");
        }
        this.config = config;
        this.parser = parser;
        ClassLoader actual = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        if (actual == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Meta-sitemap classLoader is required");
        }
        this.classLoader = actual;
    }

    /**
     * 加载 meta-sitemap 文档。
     */
    public SitemapConfig load() {
        String resourcePath = config.resourcePath();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw ConsoleExceptions.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "Meta-sitemap resource not found: " + resourcePath);
            }
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return parser.parse(content);
        } catch (IOException exception) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read meta-sitemap resource: " + resourcePath,
                    exception);
        }
    }
}

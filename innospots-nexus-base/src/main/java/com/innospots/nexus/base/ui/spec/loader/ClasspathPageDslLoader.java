package com.innospots.nexus.base.ui.spec.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.config.PageDslConfig;
import com.innospots.nexus.base.ui.spec.parser.PageDslParser;

/**
 * Loads page DSL documents from the classpath using {@link com.innospots.nexus.base.ui.spec.config.PageDslConfig}.
 *
 * <p>After parsing, the loader verifies that {@code page.id} matches the requested
 * {@code pageKey} to prevent module/page resource mismatches.</p>
 */
public final class ClasspathPageDslLoader implements PageDslLoader {

    private final PageDslConfig config;
    private final PageDslParser parser;
    private final ClassLoader classLoader;

    /**
     * Creates a classpath loader.
     *
     * @param config resource location configuration
     * @param parser YAML parser
     * @param classLoader class loader, or {@code null} to use the current thread context loader
     */
    public ClasspathPageDslLoader(
            PageDslConfig config,
            PageDslParser parser,
            ClassLoader classLoader
    ) {
        if (config == null || parser == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl config and parser are required");
        }
        this.config = config;
        this.parser = parser;
        ClassLoader actual = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
        if (actual == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl classLoader is required");
        }
        this.classLoader = actual;
    }

    @Override
    public PageDsl load(String moduleKey, String pageKey) {
        String resourcePath = config.resourcePath(moduleKey, pageKey);
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "PageDsl resource not found: " + resourcePath);
            }
            String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            PageDsl document = parser.parse(content);
            if (document.getPage() == null || !pageKey.equals(document.getPage().getId())) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "PageDsl page.id does not match pageKey: " + resourcePath);
            }
            return document;
        } catch (IOException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read PageDsl resource: " + resourcePath,
                    exception);
        }
    }
}

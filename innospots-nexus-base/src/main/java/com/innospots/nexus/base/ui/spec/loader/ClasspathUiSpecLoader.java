package com.innospots.nexus.base.ui.spec.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.config.UiSpecConfig;
import com.innospots.nexus.base.ui.spec.parser.UiSpecParser;

/** Loads page specifications from a configurable classpath directory. */
public final class ClasspathUiSpecLoader implements UiSpecLoader {

    private final UiSpecConfig config;
    private final UiSpecParser parser;
    private final ClassLoader classLoader;

    /** Creates a classpath loader with explicit dependencies. */
    public ClasspathUiSpecLoader(
            UiSpecConfig config,
            UiSpecParser parser,
            ClassLoader classLoader
    ) {
        if (config == null || parser == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec config and parser are required");
        }
        this.config = config;
        this.parser = parser;
        this.classLoader = classLoader == null
                ? Thread.currentThread().getContextClassLoader()
                : classLoader;
        if (this.classLoader == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "UiSpec classLoader is required");
        }
    }

    /** Loads and parses one classpath page specification. */
    @Override
    public UiSpec load(String moduleKey, String pageKey) {
        String resourcePath = config.resourcePath(moduleKey, pageKey);
        try (InputStream input = classLoader.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw NexusException.build(
                        NexusStatusCode.RESOURCE_NOT_FOUND.fullCode(),
                        "UiSpec resource not found: " + resourcePath);
            }
            String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            UiSpec spec = parser.parse(content);
            if (!pageKey.equals(spec.pageInfo().pageId())) {
                throw NexusException.build(
                        NexusStatusCode.CONFIG_ERROR.fullCode(),
                        "UiSpec pageId does not match pageKey: " + resourcePath);
            }
            return spec;
        } catch (IOException exception) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Cannot read UiSpec resource: " + resourcePath,
                    exception);
        }
    }
}

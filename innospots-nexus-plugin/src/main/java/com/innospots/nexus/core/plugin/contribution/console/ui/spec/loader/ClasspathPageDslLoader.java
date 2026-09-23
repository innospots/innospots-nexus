package com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.parser.PageDslParser;

/**
 * 使用
 * {@link com.innospots.nexus.core.plugin.contribution.console.ui.spec.config.PageDslConfig}
 * 从 classpath 加载页面 DSL 文档。
 *
 * <p>解析后，加载器校验 {@code page.id} 与请求的 {@code pageKey} 是否一致，以防模块/页面
 * 资源不匹配。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class ClasspathPageDslLoader implements PageDslLoader {

    private final PageDslConfig config;
    private final PageDslParser parser;
    private final ClassLoader classLoader;

    /**
     * 创建 classpath 加载器。
     *
     * @param config 资源定位配置
     * @param parser YAML 解析器
     * @param classLoader 类加载器；为 {@code null} 时使用当前线程上下文类加载器
     * @throws com.innospots.nexus.base.exception.NexusException 配置、解析器或类加载器缺失时
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

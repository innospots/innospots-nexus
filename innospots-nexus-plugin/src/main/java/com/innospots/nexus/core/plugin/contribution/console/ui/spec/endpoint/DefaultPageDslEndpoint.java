package com.innospots.nexus.core.plugin.contribution.console.ui.spec.endpoint;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter.PageDslFilterChain;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter.PageDslRenderContext;
import com.innospots.nexus.core.plugin.contribution.console.ui.spec.loader.PageDslLoader;

import java.util.Map;

/**
 * 默认 {@link PageDslEndpoint} 实现：从 classpath 加载页面 DSL 文档，并通过已配置的过滤器链处理。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class DefaultPageDslEndpoint implements PageDslEndpoint {

    private final PageDslLoader loader;
    private final PageDslFilterChain filterChain;

    /**
     * 使用提供的加载器与过滤器链创建端点。
     *
     * @param loader 页面 DSL 加载器
     * @param filterChain 有序渲染时过滤器
     */
    public DefaultPageDslEndpoint(PageDslLoader loader, PageDslFilterChain filterChain) {
        if (loader == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl loader is required");
        }
        this.loader = loader;
        this.filterChain = filterChain == null ? PageDslFilterChain.create() : filterChain;
    }

    @Override
    public PageDsl render(String moduleKey, String pageKey, Map<String, Object> parameters) {
        if (!hasText(moduleKey) || !hasText(pageKey)) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "PageDsl moduleKey and pageKey are required");
        }
        PageDsl document = loader.load(moduleKey, pageKey);
        PageDslRenderContext context = PageDslRenderContext.of(moduleKey, pageKey, document, parameters);
        return filterChain.process(context);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

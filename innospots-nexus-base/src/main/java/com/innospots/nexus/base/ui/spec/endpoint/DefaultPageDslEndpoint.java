package com.innospots.nexus.base.ui.spec.endpoint;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.ui.spec.PageDsl;
import com.innospots.nexus.base.ui.spec.filter.PageDslFilterChain;
import com.innospots.nexus.base.ui.spec.filter.PageDslRenderContext;
import com.innospots.nexus.base.ui.spec.loader.PageDslLoader;

import java.util.Map;

/**
 * Default {@link PageDslEndpoint} that loads a classpath page DSL document and
 * runs it through a configured filter chain.
 */
public final class DefaultPageDslEndpoint implements PageDslEndpoint {

    private final PageDslLoader loader;
    private final PageDslFilterChain filterChain;

    /**
     * Creates an endpoint with the supplied loader and filter chain.
     *
     * @param loader page DSL loader
     * @param filterChain ordered render-time filters
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

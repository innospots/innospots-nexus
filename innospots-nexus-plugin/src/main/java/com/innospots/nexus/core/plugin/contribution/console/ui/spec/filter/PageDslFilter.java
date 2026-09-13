package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/**
 * Transforms a page DSL document during render-time preparation.
 *
 * <p>Implementations must return a non-null document. Filters may mutate the working document
 * in place when the transformation is scoped to the current request.</p>
 *
 * @see PageDslFilterChain
 * @see PageDslRenderContext
 */
@FunctionalInterface
public interface PageDslFilter {

    /**
     * Applies one transformation step and returns the document for the next filter.
     *
     * @param context current render context
     * @return transformed document; must not be {@code null}
     */
    PageDsl filter(PageDslRenderContext context);

    /**
     * Returns a stable identifier for logging and diagnostics.
     *
     * @return filter identifier
     */
    default String filterId() {
        return getClass().getSimpleName();
    }
}

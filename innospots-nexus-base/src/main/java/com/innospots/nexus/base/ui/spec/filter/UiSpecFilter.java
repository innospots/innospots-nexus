package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.ui.spec.UiSpec;

/**
 * Transforms a page specification during render-time preparation.
 * Implementations may enforce permissions, bind variables, remove elements,
 * or enrich the page structure before the result is sent to the frontend.
 *
 * @see UiSpecFilterChain
 * @see UiSpecRenderContext
 */
@FunctionalInterface
public interface UiSpecFilter {

    /**
     * Applies one transformation step and returns the specification for the
     * next filter in the chain.
     *
     * @param context current render context carrying the working specification
     * @return transformed specification; must not be {@code null}
     */
    UiSpec filter(UiSpecRenderContext context);

    /**
     * Returns a stable identifier for logging and diagnostics.
     *
     * @return filter identifier
     */
    default String filterId() {
        return getClass().getSimpleName();
    }
}

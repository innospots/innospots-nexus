package com.innospots.nexus.core.plugin.contribution.console.ui.spec.filter;

import com.innospots.nexus.core.plugin.contribution.console.ui.spec.PageDsl;

/**
 * Binds request parameters into the page DSL {@code state} map.
 *
 * <p>Request parameters override existing state keys because render-time filters typically run
 * after the document is loaded from storage.</p>
 */
public final class StateBindingPageDslFilter implements PageDslFilter {

    /** Creates a state-binding filter. */
    public StateBindingPageDslFilter() {
    }

    @Override
    public PageDsl filter(PageDslRenderContext context) {
        PageDsl document = context.document();
        document.bindState(context.parameters());
        return document;
    }
}

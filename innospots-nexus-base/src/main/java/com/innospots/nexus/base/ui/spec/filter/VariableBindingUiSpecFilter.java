package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.ui.spec.UiSpec;

/**
 * Binds request parameters into the page specification variable declarations.
 */
public final class VariableBindingUiSpecFilter implements UiSpecFilter {

    /** Creates a variable-binding filter. */
    public VariableBindingUiSpecFilter() {
    }

    @Override
    public UiSpec filter(UiSpecRenderContext context) {
        UiSpec spec = context.spec();
        spec.addVariableValues(context.parameters());
        return spec;
    }
}

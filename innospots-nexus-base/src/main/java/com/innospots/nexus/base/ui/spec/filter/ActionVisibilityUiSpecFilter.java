package com.innospots.nexus.base.ui.spec.filter;

import com.innospots.nexus.base.ui.spec.UiSpec;
import com.innospots.nexus.base.ui.spec.Variable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Filters {@code actionDefinitions} whose {@code visibleIf} expression evaluates
 * to {@code false}. Variable values are resolved from the current specification
 * and request parameters.
 */
public final class ActionVisibilityUiSpecFilter implements UiSpecFilter {

    private final BiFunction<String, Map<String, Object>, Boolean> evaluator;

    /**
     * Creates a filter using the supplied expression evaluator.
     *
     * @param evaluator receives a normalized expression and variable values
     */
    public ActionVisibilityUiSpecFilter(BiFunction<String, Map<String, Object>, Boolean> evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public UiSpec filter(UiSpecRenderContext context) {
        UiSpec spec = context.spec();
        if (evaluator == null) {
            return spec;
        }
        spec.filterActionDefinitions(evaluator);
        return spec;
    }

    /**
     * Builds a variable map from declared defaults and request parameters.
     * Request parameters override declared defaults.
     *
     * @param spec page specification
     * @param parameters runtime parameters
     * @return merged variable values
     */
    public static Map<String, Object> variableValues(UiSpec spec, Map<String, Object> parameters) {
        Map<String, Object> values = new LinkedHashMap<>();
        if (spec != null) {
            for (Map.Entry<String, Variable> entry : spec.variables().entrySet()) {
                values.put(entry.getKey(), entry.getValue().defaultValue());
            }
        }
        if (parameters != null) {
            values.putAll(parameters);
        }
        return Map.copyOf(values);
    }
}

package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.datasource.UiDatasource;
import com.innospots.nexus.base.ui.spec.form.SelectOptions;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Complete page specification consumed by management-platform renderers.
 * The specification is framework-neutral and is loaded from YAML.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiSpec {

    private PageInfo pageInfo;
    private String pageType = "general";
    private Map<String, Object> meta = new LinkedHashMap<>();
    private Map<String, Variable> variables = new LinkedHashMap<>();
    private Map<String, UiDatasource> datasources = new LinkedHashMap<>();
    private Map<String, UiComponentSpec> components = new LinkedHashMap<>();
    private UiLayout layout;
    private Map<String, UiAction> actionDefinitions = new LinkedHashMap<>();
    private Map<String, UiAction> aiActions = new LinkedHashMap<>();
    private Map<String, SelectOptions> optionSources = new LinkedHashMap<>();

    /** Creates an empty page specification for deserialization or fluent assembly. */
    public UiSpec() {
    }

    /**
     * Creates an empty page specification.
     *
     * @return empty specification
     */
    public static UiSpec create() {
        return new UiSpec();
    }

    /**
     * Creates a specification for the supplied page.
     *
     * @param pageInfo page identity and display metadata
     * @return page specification
     */
    public static UiSpec page(PageInfo pageInfo) {
        UiSpec spec = new UiSpec();
        spec.pageInfo = pageInfo;
        return spec;
    }

    public PageInfo pageInfo() {
        return pageInfo;
    }

    public String pageType() {
        return pageType;
    }

    public UiSpec pageType(String pageType) {
        this.pageType = pageType;
        return this;
    }

    public Map<String, Object> meta() {
        return Map.copyOf(meta);
    }

    public UiSpec meta(String key, Object value) {
        if (key != null) {
            meta.put(key, value);
        }
        return this;
    }

    /** Returns an immutable view of declared variables. */
    public Map<String, Variable> variables() {
        return Map.copyOf(variables);
    }

    /** Adds or replaces a variable declaration. */
    public UiSpec variable(String key, Variable variable) {
        if (key != null && variable != null) {
            variables.put(key, variable);
        }
        return this;
    }

    /**
     * Applies runtime values to declared variables, creating declarations for
     * previously unknown values.
     *
     * @param values variable values
     */
    public void addVariableValues(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            variables.compute(entry.getKey(), (key, variable) -> {
                Variable actual = variable == null ? Variable.of(key, entry.getValue()) : variable;
                actual.defaultValue(entry.getValue());
                return actual;
            });
        }
    }

    /** Returns an immutable view of datasource declarations. */
    public Map<String, UiDatasource> datasources() {
        return Map.copyOf(datasources);
    }

    /** Adds or replaces a datasource declaration. */
    public UiSpec datasource(String key, UiDatasource datasource) {
        if (key != null && datasource != null) {
            datasources.put(key, datasource);
        }
        return this;
    }

    /** Adds a datasource converted from a simple API request. */
    public UiSpec datasource(String key, ApiRequest request) {
        return request == null ? this : datasource(key, UiDatasource.from(request));
    }

    public Map<String, UiComponentSpec> components() {
        return Map.copyOf(components);
    }

    public UiSpec component(UiComponentSpec component) {
        if (component != null) {
            components.put(component.componentId(), component);
        }
        return this;
    }

    public UiLayout layout() {
        return layout;
    }

    public UiSpec layout(UiLayout layout) {
        this.layout = layout;
        return this;
    }

    public Map<String, UiAction> actionDefinitions() {
        return Map.copyOf(actionDefinitions);
    }

    public UiSpec actionDefinition(UiAction action) {
        if (action != null) {
            actionDefinitions.put(action.actionId(), action);
        }
        return this;
    }

    /** Returns an immutable view of AI action declarations. */
    public Map<String, UiAction> aiActions() {
        return Map.copyOf(aiActions);
    }

    /** Adds or replaces an AI action declaration. */
    public UiSpec aiAction(UiAction action) {
        if (action != null) {
            aiActions.put(action.actionId(), action);
        }
        return this;
    }

    /**
     * Removes actions whose visible condition evaluates to false.
     *
     * @param evaluator expression evaluator receiving normalized expression and variables
     */
    public void filterActionDefinitions(BiFunction<String, Map<String, Object>, Boolean> evaluator) {
        if (evaluator == null || actionDefinitions.isEmpty()) {
            return;
        }
        Map<String, Object> context = new LinkedHashMap<>();
        variables.forEach((key, variable) -> context.put(key, variable.defaultValue()));
        Iterator<Map.Entry<String, UiAction>> iterator = actionDefinitions.entrySet().iterator();
        while (iterator.hasNext()) {
            UiAction action = iterator.next().getValue();
            if (action.visibleIf() != null && action.visibleIf().expression() != null) {
                String expression = normalizeExpression(action.visibleIf().expression());
                if (!Boolean.TRUE.equals(evaluator.apply(expression, Map.copyOf(context)))) {
                    iterator.remove();
                }
            }
        }
    }

    public Map<String, SelectOptions> optionSources() {
        return Map.copyOf(optionSources);
    }

    public UiSpec optionSource(String key, SelectOptions options) {
        if (key != null && options != null) {
            optionSources.put(key, options);
        }
        return this;
    }

    private static String normalizeExpression(String expression) {
        return expression.replaceAll("\\$\\{([^}]+)}", "$1");
    }
}

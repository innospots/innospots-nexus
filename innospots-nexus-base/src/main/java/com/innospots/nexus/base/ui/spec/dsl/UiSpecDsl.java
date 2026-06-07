package com.innospots.nexus.base.ui.spec.dsl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.ApiRequest;
import com.innospots.nexus.base.ui.spec.PageInfo;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.form.SelectOptions;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiSpecDsl {

    private PageInfo pageInfo;
    private String type = "general";
    private Map<String, Object> meta = new LinkedHashMap<>();
    private Map<String, Variable> variables = new LinkedHashMap<>();
    private Map<String, ApiRequest> datasources = new LinkedHashMap<>();
    private Map<String, UiComponentSpec> components = new LinkedHashMap<>();
    private UiLayout layout;
    private Map<String, UiAction> actionDefinitions = new LinkedHashMap<>();
    private Map<String, UiAction> aiActions = new LinkedHashMap<>();
    private Map<String, SelectOptions> optionSources = new LinkedHashMap<>();

    public UiSpecDsl() {
    }

    public static UiSpecDsl create() {
        return new UiSpecDsl();
    }

    public PageInfo pageInfo() {
        return pageInfo;
    }

    public UiSpecDsl pageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
        return this;
    }

    public String type() {
        return type;
    }

    public UiSpecDsl type(String type) {
        this.type = type;
        return this;
    }

    public Map<String, Object> meta() {
        return Map.copyOf(meta);
    }

    public UiSpecDsl meta(String key, Object value) {
        if (key != null) {
            meta.put(key, value);
        }
        return this;
    }

    public Map<String, Variable> variables() {
        return Map.copyOf(variables);
    }

    public UiSpecDsl variable(String key, Variable variable) {
        if (key != null && variable != null) {
            variables.put(key, variable);
        }
        return this;
    }

    public void addVariableValues(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            variables.compute(entry.getKey(), (key, variable) -> {
                Variable actual = variable == null ? Variable.of(key, entry.getValue()) : variable;
                actual.defaultValue(entry.getValue());
                return actual;
            });
        }
    }

    public Map<String, ApiRequest> datasources() {
        return Map.copyOf(datasources);
    }

    public UiSpecDsl datasource(String key, ApiRequest request) {
        if (key != null && request != null) {
            datasources.put(key, request);
        }
        return this;
    }

    public Map<String, UiComponentSpec> components() {
        return Map.copyOf(components);
    }

    public UiSpecDsl component(UiComponentSpec component) {
        if (component != null) {
            components.put(component.componentId(), component);
        }
        return this;
    }

    public UiLayout layout() {
        return layout;
    }

    public UiSpecDsl layout(UiLayout layout) {
        this.layout = layout;
        return this;
    }

    public Map<String, UiAction> actionDefinitions() {
        return Map.copyOf(actionDefinitions);
    }

    public UiSpecDsl actionDefinition(UiAction action) {
        if (action != null) {
            actionDefinitions.put(action.actionId(), action);
        }
        return this;
    }

    public Map<String, UiAction> aiActions() {
        return Map.copyOf(aiActions);
    }

    public UiSpecDsl aiAction(UiAction action) {
        if (action != null) {
            aiActions.put(action.actionId(), action);
        }
        return this;
    }

    public Map<String, SelectOptions> optionSources() {
        return Map.copyOf(optionSources);
    }

    public UiSpecDsl optionSource(String key, SelectOptions options) {
        if (key != null && options != null) {
            optionSources.put(key, options);
        }
        return this;
    }

    public void filterActionDefinitions(BiFunction<String, Map<String, Object>, Boolean> evaluator) {
        if (evaluator == null || actionDefinitions.isEmpty()) {
            return;
        }
        Map<String, Object> context = new LinkedHashMap<>();
        variables.forEach((key, variable) -> context.put(key, variable.defaultValue()));
        Iterator<Map.Entry<String, UiAction>> iterator = actionDefinitions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, UiAction> entry = iterator.next();
            UiAction action = entry.getValue();
            if (action.visibleIf() != null && action.visibleIf().expression() != null) {
                String expression = normalizeExpression(action.visibleIf().expression());
                Boolean visible = evaluator.apply(expression, context);
                if (!Boolean.TRUE.equals(visible)) {
                    iterator.remove();
                }
            }
        }
    }

    private static String normalizeExpression(String expression) {
        return expression.replaceAll("\\$\\{([^}]+)}", "$1");
    }
}

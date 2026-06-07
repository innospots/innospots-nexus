package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.component.UiComponentSpec;
import com.innospots.nexus.base.ui.spec.form.SelectOptions;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiSpec {

    private PageInfo pageInfo;
    private String pageType = "general";
    private Map<String, Object> meta = new LinkedHashMap<>();
    private Map<String, Object> variables = new LinkedHashMap<>();
    private Map<String, ApiRequest> datasources = new LinkedHashMap<>();
    private Map<String, UiComponentSpec> components = new LinkedHashMap<>();
    private UiLayout layout;
    private Map<String, UiAction> actionDefinitions = new LinkedHashMap<>();
    private Map<String, SelectOptions> optionSources = new LinkedHashMap<>();

    public UiSpec() {
    }

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

    public Map<String, Object> variables() {
        return Map.copyOf(variables);
    }

    public UiSpec variable(String key, Object value) {
        if (key != null) {
            variables.put(key, value);
        }
        return this;
    }

    public Map<String, ApiRequest> datasources() {
        return Map.copyOf(datasources);
    }

    public UiSpec datasource(String key, ApiRequest request) {
        if (key != null && request != null) {
            datasources.put(key, request);
        }
        return this;
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

    public Map<String, SelectOptions> optionSources() {
        return Map.copyOf(optionSources);
    }

    public UiSpec optionSource(String key, SelectOptions options) {
        if (key != null && options != null) {
            optionSources.put(key, options);
        }
        return this;
    }
}

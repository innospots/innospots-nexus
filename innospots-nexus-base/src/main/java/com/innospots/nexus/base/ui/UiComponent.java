package com.innospots.nexus.base.ui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A configurable UI component descriptor used in dynamic page definitions.
 * Carries an identifier, display name, component type, value type,
 * settings map, layout hints, and conditional visibility rules.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiComponent {

    private String componentId;
    private String name;
    private String type;
    private String valueType;
    private boolean copyable;
    private boolean deletable;
    private Map<String, Object> settings = new LinkedHashMap<>();
    private Map<String, Object> layout = new LinkedHashMap<>();
    private List<UiCondition> conditions = List.of();

    public UiComponent() {
    }

    public static UiComponent named(String name, String type) {
        UiComponent component = new UiComponent();
        component.componentId = "c_" + StringUtils.randomKey(8);
        component.name = name;
        component.type = type;
        return component;
    }

    public String componentId() {
        return componentId;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public String valueType() {
        return valueType;
    }

    public UiComponent valueType(String valueType) {
        this.valueType = valueType;
        return this;
    }

    public boolean copyable() {
        return copyable;
    }

    public UiComponent copyable(boolean copyable) {
        this.copyable = copyable;
        return this;
    }

    public boolean deletable() {
        return deletable;
    }

    public UiComponent deletable(boolean deletable) {
        this.deletable = deletable;
        return this;
    }

    public Map<String, Object> settings() {
        return Map.copyOf(settings);
    }

    public UiComponent setting(String key, Object value) {
        if (key != null) {
            settings.put(key, value);
        }
        return this;
    }

    public Map<String, Object> layout() {
        return Map.copyOf(layout);
    }

    public UiComponent layout(String key, Object value) {
        if (key != null) {
            layout.put(key, value);
        }
        return this;
    }

    public List<UiCondition> conditions() {
        return conditions;
    }

    public UiComponent conditions(List<UiCondition> conditions) {
        this.conditions = conditions == null ? List.of() : List.copyOf(conditions);
        return this;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", componentId);
        values.put("name", name);
        values.put("type", type);
        values.put("valueType", valueType);
        values.put("copyable", copyable);
        values.put("deletable", deletable);
        values.put("settings", settings);
        if (!layout.isEmpty()) {
            values.put("layout", layout);
        }
        if (!conditions.isEmpty()) {
            values.put("conditions", conditions);
        }
        return values;
    }
}

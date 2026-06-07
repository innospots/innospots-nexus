package com.innospots.nexus.base.ui.spec.component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.ui.spec.VisibleCondition;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import com.innospots.nexus.base.ui.spec.form.FormField;
import com.innospots.nexus.base.ui.spec.layout.UiLayout;
import com.innospots.nexus.base.ui.spec.table.UiTable;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiComponentSpec {

    private String componentId;
    private String type;
    private I18nObject title = new I18nObject();
    private I18nObject description = new I18nObject();
    private String datasource;
    private final Map<String, Object> style = new LinkedHashMap<>();
    private Object height;
    private String width;
    private VisibleCondition visibleIf;
    private String icon;
    private final List<String> permissions = new ArrayList<>();
    private UiTable table;
    private final List<FormField> formFields = new ArrayList<>();
    private UiLayout layout;
    private final List<UiAction> actions = new ArrayList<>();
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public UiComponentSpec() {
    }

    public static UiComponentSpec of(String componentId, ComponentType type) {
        UiComponentSpec component = new UiComponentSpec();
        component.componentId = componentId;
        component.type = type == null ? null : type.code();
        return component;
    }

    public String componentId() {
        return componentId;
    }

    public String type() {
        return type;
    }

    public I18nObject title() {
        return title;
    }

    public UiComponentSpec title(I18nObject title) {
        this.title = title == null ? new I18nObject() : title;
        return this;
    }

    public I18nObject description() {
        return description;
    }

    public UiComponentSpec description(I18nObject description) {
        this.description = description == null ? new I18nObject() : description;
        return this;
    }

    public String datasource() {
        return datasource;
    }

    public UiComponentSpec datasource(String datasource) {
        this.datasource = datasource;
        return this;
    }

    public Map<String, Object> style() {
        return Map.copyOf(style);
    }

    public UiComponentSpec style(String key, Object value) {
        if (key != null) {
            style.put(key, value);
        }
        return this;
    }

    public Object height() {
        return height;
    }

    public UiComponentSpec height(Object height) {
        this.height = height;
        return this;
    }

    public String width() {
        return width;
    }

    public UiComponentSpec width(String width) {
        this.width = width;
        return this;
    }

    public VisibleCondition visibleIf() {
        return visibleIf;
    }

    public UiComponentSpec visibleIf(VisibleCondition visibleIf) {
        this.visibleIf = visibleIf;
        return this;
    }

    public String icon() {
        return icon;
    }

    public UiComponentSpec icon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<String> permissions() {
        return List.copyOf(permissions);
    }

    public UiComponentSpec permission(String permission) {
        if (permission != null) {
            permissions.add(permission);
        }
        return this;
    }

    public UiTable table() {
        return table;
    }

    public UiComponentSpec table(UiTable table) {
        this.table = table;
        return this;
    }

    public List<FormField> formFields() {
        return List.copyOf(formFields);
    }

    public UiComponentSpec formField(FormField field) {
        if (field != null) {
            formFields.add(field);
        }
        return this;
    }

    public UiLayout layout() {
        return layout;
    }

    public UiComponentSpec layout(UiLayout layout) {
        this.layout = layout;
        return this;
    }

    public List<UiAction> actions() {
        return List.copyOf(actions);
    }

    public UiComponentSpec action(UiAction action) {
        if (action != null) {
            actions.add(action);
        }
        return this;
    }

    public Map<String, Object> properties() {
        return Map.copyOf(properties);
    }

    public UiComponentSpec property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}

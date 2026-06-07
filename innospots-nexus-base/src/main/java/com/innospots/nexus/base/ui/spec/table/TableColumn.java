package com.innospots.nexus.base.ui.spec.table;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.ui.spec.action.UiAction;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TableColumn {

    private String title;
    private I18nObject label = new I18nObject();
    private String field;
    private String valueType;
    private Boolean sortable;
    private Boolean copyable;
    private Integer width;
    private UiAction action;
    private Map<String, Object> properties = new LinkedHashMap<>();

    public TableColumn() {
    }

    public static TableColumn named(String title, String field, String valueType) {
        TableColumn column = new TableColumn();
        column.title = title;
        column.label = I18nObject.of(title);
        column.field = field;
        column.valueType = valueType;
        return column;
    }

    public String title() {
        return title;
    }

    public I18nObject label() {
        return label;
    }

    public String field() {
        return field;
    }

    public String valueType() {
        return valueType;
    }

    public TableColumn sortable(Boolean sortable) {
        this.sortable = sortable;
        return this;
    }

    public TableColumn copyable(Boolean copyable) {
        this.copyable = copyable;
        return this;
    }

    public TableColumn width(Integer width) {
        this.width = width;
        return this;
    }

    public TableColumn action(UiAction action) {
        this.action = action;
        return this;
    }

    public TableColumn property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}

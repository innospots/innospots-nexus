package com.innospots.nexus.base.ui.spec.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.ui.spec.VisibleCondition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FormField {

    private String field;
    private String name;
    private I18nObject label = new I18nObject();
    private String type;
    private Boolean required;
    private I18nObject placeholder = new I18nObject();
    private List<OptionItem> options = new ArrayList<>();
    private String datasource;
    private String valueField;
    private String labelField;
    private FormFieldValidation validation;
    private Integer maxLength;
    private Integer span;
    private Object defaultValue;
    private Boolean hidden;
    private VisibleCondition visibleIf;
    private Boolean multiple;
    private String mode;
    private String optionSource;
    private String selectionType;
    private Integer min;
    private Integer max;
    private String format;
    private String layout;
    private Boolean readonly;
    private Boolean copyable;
    private Map<String, Object> style = new LinkedHashMap<>();

    public FormField() {
    }

    public static FormField named(String field, String name, String type) {
        FormField formField = new FormField();
        formField.field = field;
        formField.name = name;
        formField.type = type;
        return formField;
    }

    public String field() {
        return field;
    }

    public String name() {
        return name;
    }

    public I18nObject label() {
        return label;
    }

    public FormField label(I18nObject label) {
        this.label = label == null ? new I18nObject() : label;
        return this;
    }

    public String type() {
        return type;
    }

    public Boolean required() {
        return required;
    }

    public FormField required(Boolean required) {
        this.required = required;
        return this;
    }

    public I18nObject placeholder() {
        return placeholder;
    }

    public FormField placeholder(I18nObject placeholder) {
        this.placeholder = placeholder == null ? new I18nObject() : placeholder;
        return this;
    }

    public List<OptionItem> options() {
        return List.copyOf(options);
    }

    public FormField option(OptionItem option) {
        if (option != null) {
            options.add(option);
        }
        return this;
    }

    public FormFieldValidation validation() {
        return validation;
    }

    public FormField validation(FormFieldValidation validation) {
        this.validation = validation;
        return this;
    }

    public VisibleCondition visibleIf() {
        return visibleIf;
    }

    public FormField visibleIf(VisibleCondition visibleIf) {
        this.visibleIf = visibleIf;
        return this;
    }

    public FormField property(String key, Object value) {
        if ("datasource".equals(key)) {
            datasource = String.valueOf(value);
        } else if ("valueField".equals(key)) {
            valueField = String.valueOf(value);
        } else if ("labelField".equals(key)) {
            labelField = String.valueOf(value);
        } else if ("maxLength".equals(key) && value instanceof Integer integer) {
            maxLength = integer;
        } else if ("span".equals(key) && value instanceof Integer integer) {
            span = integer;
        } else if ("defaultValue".equals(key)) {
            defaultValue = value;
        } else if ("hidden".equals(key) && value instanceof Boolean bool) {
            hidden = bool;
        } else if ("multiple".equals(key) && value instanceof Boolean bool) {
            multiple = bool;
        } else if ("mode".equals(key)) {
            mode = String.valueOf(value);
        } else if ("optionSource".equals(key)) {
            optionSource = String.valueOf(value);
        } else if ("selectionType".equals(key)) {
            selectionType = String.valueOf(value);
        } else if ("min".equals(key) && value instanceof Integer integer) {
            min = integer;
        } else if ("max".equals(key) && value instanceof Integer integer) {
            max = integer;
        } else if ("format".equals(key)) {
            format = String.valueOf(value);
        } else if ("layout".equals(key)) {
            layout = String.valueOf(value);
        } else if ("readonly".equals(key) && value instanceof Boolean bool) {
            readonly = bool;
        } else if ("copyable".equals(key) && value instanceof Boolean bool) {
            copyable = bool;
        } else if (key != null) {
            style.put(key, value);
        }
        return this;
    }
}

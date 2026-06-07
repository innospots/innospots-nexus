package com.innospots.nexus.base.ui.spec.dsl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Variable {

    private String name;
    private String type;
    private Object defaultValue;
    private Boolean required;

    public Variable() {
    }

    public static Variable of(String name, Object defaultValue) {
        Variable variable = new Variable();
        variable.name = name;
        variable.defaultValue = defaultValue;
        return variable;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public Variable type(String type) {
        this.type = type;
        return this;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public Variable defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Boolean required() {
        return required;
    }

    public Variable required(Boolean required) {
        this.required = required;
        return this;
    }
}

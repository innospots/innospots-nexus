package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

/** Variable declaration available to page conditions and request templates. */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Variable {

    private String name;
    private String type;
    private Object defaultValue;
    private Boolean required;

    /** Creates an empty variable for deserialization. */
    public Variable() {
    }

    /** Creates a variable with a default value. */
    public static Variable of(String name, Object defaultValue) {
        Variable variable = new Variable();
        variable.name = name;
        variable.defaultValue = defaultValue;
        return variable;
    }

    /** Returns the variable name. */
    public String name() {
        return name;
    }

    /** Returns the declared value type. */
    public String type() {
        return type;
    }

    /** Sets the declared value type. */
    public Variable type(String type) {
        this.type = type;
        return this;
    }

    /** Returns the default value. */
    public Object defaultValue() {
        return defaultValue;
    }

    /** Sets the default value. */
    public Variable defaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /** Returns whether a value is required. */
    public Boolean required() {
        return required;
    }

    /** Sets whether a value is required. */
    public Variable required(Boolean required) {
        this.required = required;
        return this;
    }
}

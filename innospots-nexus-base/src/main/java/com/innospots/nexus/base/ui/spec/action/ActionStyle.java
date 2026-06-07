package com.innospots.nexus.base.ui.spec.action;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ActionStyle {

    private String variant;
    private String color;
    private String size;
    private Map<String, Object> properties = new LinkedHashMap<>();

    public ActionStyle() {
    }

    public static ActionStyle of(String variant) {
        ActionStyle style = new ActionStyle();
        style.variant = variant;
        return style;
    }

    public String variant() {
        return variant;
    }

    public String color() {
        return color;
    }

    public ActionStyle color(String color) {
        this.color = color;
        return this;
    }

    public String size() {
        return size;
    }

    public ActionStyle size(String size) {
        this.size = size;
        return this;
    }

    public Map<String, Object> properties() {
        return Map.copyOf(properties);
    }

    public ActionStyle property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}

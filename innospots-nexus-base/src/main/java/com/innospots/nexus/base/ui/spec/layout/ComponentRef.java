package com.innospots.nexus.base.ui.spec.layout;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ComponentRef {

    private String componentId;
    private Integer span;
    private String area;
    private Map<String, Object> properties = new LinkedHashMap<>();

    public ComponentRef() {
    }

    public static ComponentRef of(String componentId) {
        ComponentRef ref = new ComponentRef();
        ref.componentId = componentId;
        return ref;
    }

    public String componentId() {
        return componentId;
    }

    public Integer span() {
        return span;
    }

    public ComponentRef span(Integer span) {
        this.span = span;
        return this;
    }

    public String area() {
        return area;
    }

    public ComponentRef area(String area) {
        this.area = area;
        return this;
    }

    public Map<String, Object> properties() {
        return Map.copyOf(properties);
    }

    public ComponentRef property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}

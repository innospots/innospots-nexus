package com.innospots.nexus.base.ui.spec.layout;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiLayout {

    private String layoutId;
    private String type;
    private Integer gap;
    private List<ComponentRef> components = new ArrayList<>();
    private Map<String, Object> properties = new LinkedHashMap<>();

    public UiLayout() {
    }

    public static UiLayout of(String layoutId, LayoutType type) {
        UiLayout layout = new UiLayout();
        layout.layoutId = layoutId;
        layout.type = type == null ? null : type.code();
        return layout;
    }

    public String layoutId() {
        return layoutId;
    }

    public String type() {
        return type;
    }

    public Integer gap() {
        return gap;
    }

    public UiLayout gap(Integer gap) {
        this.gap = gap;
        return this;
    }

    public List<ComponentRef> components() {
        return List.copyOf(components);
    }

    public UiLayout component(ComponentRef component) {
        if (component != null) {
            components.add(component);
        }
        return this;
    }

    public Map<String, Object> properties() {
        return Map.copyOf(properties);
    }

    public UiLayout property(String key, Object value) {
        if (key != null) {
            properties.put(key, value);
        }
        return this;
    }
}

package com.innospots.nexus.base.ui.spec.form;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.base.i18n.I18nObject;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OptionItem {

    private Object value;
    private I18nObject label = new I18nObject();
    private String icon;
    private boolean disabled;
    private Map<String, Object> metadata = new LinkedHashMap<>();

    public OptionItem() {
    }

    public static OptionItem of(Object value, I18nObject label) {
        OptionItem item = new OptionItem();
        item.value = value;
        item.label = label == null ? new I18nObject() : label;
        return item;
    }

    public Object value() {
        return value;
    }

    public I18nObject label() {
        return label;
    }

    public String icon() {
        return icon;
    }

    public OptionItem icon(String icon) {
        this.icon = icon;
        return this;
    }

    public boolean disabled() {
        return disabled;
    }

    public OptionItem disabled(boolean disabled) {
        this.disabled = disabled;
        return this;
    }

    public Map<String, Object> metadata() {
        return Map.copyOf(metadata);
    }

    public OptionItem metadata(String key, Object value) {
        if (key != null) {
            metadata.put(key, value);
        }
        return this;
    }
}

package com.innospots.nexus.base.ui;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregate model for a multi-page UI resource definition. Contains a
 * list of {@link UiPage}s and a map of data schemas.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiResource {

    private List<UiPage> pages = new ArrayList<>();
    private Map<String, Object> schemas = new LinkedHashMap<>();

    public UiResource() {
    }

    public static UiResource create() {
        return new UiResource();
    }

    public List<UiPage> pages() {
        return List.copyOf(pages);
    }

    public UiResource page(UiPage page) {
        if (page != null) {
            pages.add(page);
        }
        return this;
    }

    public Map<String, Object> schemas() {
        return Map.copyOf(schemas);
    }

    public UiResource schema(String key, Object schema) {
        if (key != null) {
            schemas.put(key, schema);
        }
        return this;
    }
}

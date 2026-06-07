package com.innospots.nexus.base.domain.data;

import com.innospots.nexus.base.domain.field.DomainField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Describes the structure of a data payload: a list of {@link DomainField}s
 * plus free-form configuration entries. Used to convey field metadata
 * alongside data responses.
 */
public class DataSchema {

    private final String code;
    private final String name;
    private final List<DomainField> fields = new ArrayList<>();
    private final Map<String, Object> configs = new LinkedHashMap<>();

    private DataSchema(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Creates a schema with a programmatic code and display name.
     */
    public static DataSchema named(String code, String name) {
        return new DataSchema(code, name);
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public List<DomainField> fields() {
        return List.copyOf(fields);
    }

    /**
     * Adds a field to the schema definition.
     */
    public DataSchema field(DomainField field) {
        if (field != null) {
            fields.add(field);
        }
        return this;
    }

    /**
     * Looks up a field by its programmatic code.
     */
    public Optional<DomainField> field(String code) {
        return fields.stream()
                .filter(field -> field.code().equals(code))
                .findFirst();
    }

    public Map<String, Object> configs() {
        return Map.copyOf(configs);
    }

    /**
     * Sets a configuration property on this schema.
     */
    public DataSchema config(String key, Object value) {
        configs.put(key, value);
        return this;
    }

    /**
     * Gets a configuration property by key.
     */
    public Object config(String key) {
        return configs.get(key);
    }
}

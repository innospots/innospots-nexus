package com.innospots.nexus.base.domain.field;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a field within a domain schema or data structure. Carries
 * an identifier, display name, programmatic code, value type, scope,
 * optional comment, and a list of selectable options.
 */
public class DomainField {

    private String fieldId;
    private String name;
    private String code;
    private String valueType;
    private FieldScope scope = FieldScope.METADATA;
    private String comment;
    private final List<SelectOption> options = new ArrayList<>();

    protected DomainField(String name, String code, String valueType) {
        this.name = name;
        this.code = code;
        this.valueType = valueType;
    }

    /**
     * Creates a field with the given display name, programmatic code, and value type name.
     */
    public static DomainField named(String name, String code, String valueType) {
        return new DomainField(name, code, valueType);
    }

    public String fieldId() {
        return fieldId;
    }

    public DomainField fieldId(String fieldId) {
        this.fieldId = fieldId;
        return this;
    }

    public String name() {
        return name;
    }

    public String code() {
        return code;
    }

    public String valueType() {
        return valueType;
    }

    public FieldScope scope() {
        return scope;
    }

    public DomainField scope(FieldScope scope) {
        this.scope = scope == null ? FieldScope.METADATA : scope;
        return this;
    }

    public String comment() {
        return comment;
    }

    public DomainField comment(String comment) {
        this.comment = comment;
        return this;
    }

    public List<SelectOption> options() {
        return List.copyOf(options);
    }

    /**
     * Adds a selectable option to this field (e.g. for dropdowns).
     */
    public DomainField option(SelectOption option) {
        if (option != null) {
            options.add(option);
        }
        return this;
    }
}

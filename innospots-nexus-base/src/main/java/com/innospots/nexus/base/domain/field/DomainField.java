package com.innospots.nexus.base.domain.field;

import java.util.ArrayList;
import java.util.List;

/**
 * 描述领域模式或数据结构中的字段。携带标识符、显示名称、程序化编码、值类型、作用域、可选注释以及可选项列表。
 *
 * @author Smars
 * @date 2026/09/13
 * @see FieldValueType
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
     * 使用给定显示名称、程序化编码与值类型名称创建字段。
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
     * 向此字段添加可选项（如用于下拉框）。
     */
    public DomainField option(SelectOption option) {
        if (option != null) {
            options.add(option);
        }
        return this;
    }
}

package com.innospots.nexus.base.domain.data;

import com.innospots.nexus.base.domain.field.DomainField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 描述数据载荷结构：{@link com.innospots.nexus.base.domain.field.DomainField} 列表加自由格式配置项。用于在数据响应中传递字段元数据。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DomainField
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
     * 使用程序化编码与显示名称创建模式。
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
     * 向模式定义添加字段。
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
     * 在此模式上设置配置属性。
     */
    public DataSchema config(String key, Object value) {
        configs.put(key, value);
        return this;
    }

    /**
     * 按键获取配置属性。
     */
    public Object config(String key) {
        return configs.get(key);
    }
}

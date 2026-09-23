package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 由字段编码、运算符、值及可选值类型组成的单个过滤条件。
 * <p>以 {@code ${...}} 或 {@code %{...}} 为前缀的值视为占位符，运行时根据输入映射解析。{@link #value(Map)} 执行此解析。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see Operator
 */
@Getter
public class Factor {

    private static final String PLACEHOLDER_PREFIX_1 = "${";
    private static final String PLACEHOLDER_PREFIX_2 = "%{";
    private static final String PLACEHOLDER_SUFFIX = "}";

    private String name;
    private final String code;
    private final Operator operator;
    private final Object value;
    private final FieldValueType valueType;

    /**
     * 使用显式值类型构造因子。
     *
     * @param code      字段标识符
     * @param operator  比较运算符
     * @param value     比较值（可为占位符字符串如 "${key}"）
     * @param valueType 用于格式化/引用的显式值类型
     */
    public Factor(String code, Operator operator, Object value, FieldValueType valueType) {
        this.code = code;
        this.operator = operator;
        this.value = value;
        this.valueType = valueType;
    }

    /**
     * 根据值的 Java 类型推断值类型并创建因子。
     *
     * @param code     字段标识符
     * @param operator 比较运算符
     * @param value    比较值
     * @return 自动检测值类型的新因子
     */
    public static Factor of(String code, Operator operator, Object value) {
        return new Factor(code, operator, value, inferType(value));
    }

    /**
     * 使用显式指定的值类型创建因子。
     *
     * @param code      字段标识符
     * @param operator  比较运算符
     * @param value     比较值
     * @param valueType 值类型
     * @return 新因子
     */
    public static Factor of(String code, Operator operator, Object value, FieldValueType valueType) {
        return new Factor(code, operator, value, valueType);
    }

    /**
     * 返回显示名称。
     *
     * @return 名称
     */
    public String name() {
        return name;
    }

    /**
     * 设置显示名称。
     *
     * @param name 名称
     * @return 当前因子
     */
    public Factor name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 返回字段编码。
     *
     * @return 字段编码
     */
    public String code() {
        return code;
    }

    /**
     * 返回比较运算符。
     *
     * @return 运算符
     */
    public Operator operator() {
        return operator;
    }

    /**
     * 返回原始比较值。
     *
     * @return 比较值
     */
    public Object value() {
        return value;
    }

    /**
     * 返回值类型。
     *
     * @return 值类型
     */
    public FieldValueType valueType() {
        return valueType;
    }

    /**
     * 根据运行时输入映射解析因子值。
     * <ul>
     *   <li>占位符字符串（{@code ${key}} 或 {@code %{key}}）从输入映射提取对应条目。</li>
     *   <li>{@link FieldValueType#OBJECT} 类型将原始值本身作为输入映射的键。</li>
     *   <li>其他值原样返回。</li>
     * </ul>
     *
     * @param input 运行时键值输入映射
     * @return 解析后的值；输入缺失时返回 null
     */
    public Object value(Map<String, Object> input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        // 解析占位符语法：${key} 或 %{key} -> 从输入映射查找
        if (value instanceof String str &&
                (str.startsWith(PLACEHOLDER_PREFIX_1) || str.startsWith(PLACEHOLDER_PREFIX_2))) {
            return input.get(str.substring(2, str.length() - 1));
        }
        // OBJECT 类型值视为动态字段引用
        if (valueType == FieldValueType.OBJECT) {
            return input.get(value);
        }
        return value;
    }

    /**
     * 从占位符值字符串提取占位符键。
     * 例如 {@code "${userId}"} 返回 {@code "userId"}。
     * 若值不是占位符，返回字符串表示。
     *
     * @return 占位符键或值的字符串表示
     */
    public String valueKey() {
        if (value instanceof String str &&
                (str.startsWith(PLACEHOLDER_PREFIX_1) || str.startsWith(PLACEHOLDER_PREFIX_2))) {
            return str.substring(2, str.length() - 1);
        }
        return String.valueOf(value);
    }

    /**
     * 检查此因子是否有任何关键属性为 null（operator、valueType 或 code）。
     *
     * @return 存在 null 关键属性时返回 {@code true}
     */
    public boolean checkNull() {
        return operator == null || valueType == null || code == null;
    }

    /**
     * 检查列表中是否有任何因子的关键属性为 null。
     *
     * @param list 因子列表
     * @return 存在 null 关键属性时返回 {@code true}
     */
    public static boolean checkNull(List<Factor> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return list.stream().anyMatch(Factor::checkNull);
    }

    /**
     * 根据值的 Java 运行时类型推断 {@link FieldValueType}。
     */
    private static FieldValueType inferType(Object value) {
        return switch (value) {
            case Integer i -> FieldValueType.INTEGER;
            case Long l -> FieldValueType.LONG;
            case Double v -> FieldValueType.DOUBLE;
            case Boolean b -> FieldValueType.BOOLEAN;
            case null -> FieldValueType.STRING;
            case List<?> l -> FieldValueType.ARRAY;
            default -> FieldValueType.STRING;
        };
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", operator=" + operator +
                ", value=" + value +
                ", valueType=" + valueType +
                '}';
    }
}

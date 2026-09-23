package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 将 {@link Factor} 渲染为脚本/表达式语言（如 MVEL、SpEL）字符串。数值不加引号；字符串使用单引号。
 * {@code IN}/{@code NOT_IN} 使用 {@code include(...)}/{@code notInclude(...)}；{@code LIKE} 使用 {@code regexMatch(...)}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see IFactorStatement
 */
public class ScriptFactorStatement implements IFactorStatement {

    /**
     * 将值规范化为脚本/表达式语言输出。
     * {@code M} 后缀表示某些表达式语言（如 MVEL）中的 BigDecimal 字面量；带此后缀的值原样传递。
     *
     * @param value     待规范化的原始值
     * @param valueType 期望的值类型，可为 null
     * @param operator  运算符，用于识别范围运算符
     * @return 规范化后的值（字符串加引号，数值不加引号）
     */
    @Override
    public Object normalizeValue(Object value, FieldValueType valueType, Operator operator) {
        if (valueType == null) {
            // 未声明类型时，Number 与 BigDecimal 字面量原样传递
            if (value instanceof Number) {
                return value;
            }
            // BigDecimal MVEL 字面量（如 10.5M）
            if (value != null && value.toString().endsWith("M")) {
                return value;
            }
            // 范围运算符的值不加引号以便算术比较
            if (operator != null && operator.isRange()) {
                return value;
            }
            return "'" + value + "'";
        }
        if (valueType == FieldValueType.INTEGER || valueType == FieldValueType.LONG
                || valueType == FieldValueType.DOUBLE || valueType == FieldValueType.DECIMAL) {
            if (value instanceof Number) {
                return value;
            }
            if (value != null && value.toString().endsWith("M")) {
                return value;
            }
            return valueType.convert(value);
        }
        // 非数值类型也原样传递 BigDecimal 字面量与范围值
        if (value != null && (value.toString().endsWith("M") || (operator != null && operator.isRange()))) {
            return value;
        }
        return "'" + value + "'";
    }

    @Override
    public Object normalizeValue(Factor factor) {
        return normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * 将单个因子渲染为脚本表达式。
     * <ul>
     *   <li>Comparison operators: {@code field == value}</li>
     *   <li>IN/NOT_IN: {@code include(seq.set(v1,v2), field)}</li>
     *   <li>LIKE: {@code regexMatch(pattern, field)}</li>
     *   <li>BETWEEN: {@code field >= start && field <= end}</li>
     * </ul>
     */
    @Override
    public String statement(Factor factor) {
        if (factor.operator() == null) {
            return "";
        }
        StringBuilder stmt = new StringBuilder();
        switch (factor.operator()) {
            // 空值/一元检查：field == null / field != null（裸关键字，不加引号）
            case IS_NULL ->
                    stmt.append(factor.code()).append(" == null");
            case IS_NOT_NULL, HAS_VALUE ->
                    stmt.append(factor.code()).append(" != null");
            // 简单二元运算符：code op normalizedValue
            case EQUAL, LESS, NOT_EQUAL, GREATER, LESS_EQUAL, GREATER_EQUAL ->
                    stmt.append(factor.code()).append(factor.operator().scriptSymbol())
                            .append(normalizeValue(factor));
            // IN 映射为 include(seq.set(vals), field)
            case IN -> {
                if (factor.value() instanceof Collection<?> col) {
                    String vs = col.stream()
                            .map(v -> String.valueOf(normalizeValue(v, factor.valueType())))
                            .collect(Collectors.joining(","));
                    stmt.append("include(seq.set(").append(vs).append("),").append(factor.code()).append(")");
                } else {
                    stmt.append("include(seq.set(").append(normalizeValue(factor)).append("),").append(factor.code()).append(")");
                }
            }
            // NOT_IN 映射为 notInclude(seq.set(vals), field)
            case NOT_IN -> {
                if (factor.value() instanceof Collection<?> col) {
                    String vs = col.stream()
                            .map(v -> String.valueOf(normalizeValue(v, factor.valueType())))
                            .collect(Collectors.joining(","));
                    stmt.append("notInclude(seq.set(").append(vs).append("),").append(factor.code()).append(")");
                } else {
                    stmt.append("notInclude(seq.set(").append(normalizeValue(factor)).append("),").append(factor.code()).append(")");
                }
            }
            // LIKE 映射为 regexMatch(pattern, field)
            case LIKE -> stmt.append("regexMatch(").append(normalizeValue(factor))
                    .append(",").append(factor.code()).append(")");
            // BETWEEN 展开为 field >= start && field <= end
            case BETWEEN -> {
                if (factor.value() instanceof Collection<?> col) {
                    int i = 0;
                    String start = null, end = null;
                    for (Object v : col) {
                        if (i == 0) {
                            start = String.valueOf(normalizeValue(v, factor.valueType()));
                        } else if (i == 1) {
                            end = String.valueOf(normalizeValue(v, factor.valueType()));
                        }
                        i++;
                    }
                    stmt.append(factor.code()).append(" >= ").append(start)
                            .append(" && ").append(factor.code()).append(" <= ").append(end);
                }
            }
        }
        return stmt.toString();
    }
}

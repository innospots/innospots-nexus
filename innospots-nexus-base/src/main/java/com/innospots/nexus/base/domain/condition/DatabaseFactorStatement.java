package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将 {@link Factor} 渲染为 SQL 表达式字符串。
 * <p>值按 {@link com.innospots.nexus.base.domain.field.FieldValueType} 引用：数值与布尔类型不加引号；日期/时间类型使用 ISO 格式；字符串使用单引号并去除撇号。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see IFactorStatement
 */
public class DatabaseFactorStatement implements IFactorStatement {

    /**
     * 将值规范化为 SQL 安全字符串表示。
     * 数值与布尔类型不加引号；日期/时间类型使用 ISO 格式化；字符串使用单引号并去除撇号。
     *
     * @param value     待规范化的原始值
     * @param valueType 期望的值类型
     * @param operator  运算符（SQL 模式下未使用，保留以符合接口契约）
     * @return SQL 安全的字符串表示
     */
    @Override
    public String normalizeValue(Object value, FieldValueType valueType, Operator operator) {
        if (value == null) {
            return "";
        }
        // 数值与布尔类型不加引号
        if (valueType == FieldValueType.INTEGER || valueType == FieldValueType.LONG
                || valueType == FieldValueType.DOUBLE || valueType == FieldValueType.DECIMAL
                || valueType == FieldValueType.BOOLEAN) {
            return value.toString();
        }
        // 日期类型使用 ISO 格式并加单引号
        if (valueType == FieldValueType.DATE) {
            if (value instanceof LocalDate d) {
                return "'" + d.format(DateTimeFormatter.ISO_LOCAL_DATE) + "'";
            }
            return "'" + value + "'";
        }
        if (valueType == FieldValueType.DATE_TIME) {
            if (value instanceof LocalDateTime dt) {
                // 将 ISO 的 T 分隔符替换为 SQL 标准空格
                return "'" + dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace('T', ' ') + "'";
            }
            return "'" + value + "'";
        }
        // 其他类型（STRING、OBJECT、ARRAY）使用单引号并去除撇号
        String result = value.toString().replace("'", "");
        return "'" + result + "'";
    }

    @Override
    public Object normalizeValue(Factor factor) {
        return normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * 将单个因子渲染为完整 SQL 表达式，如
     * {@code field = 'value'} or {@code field IN (v1, v2)}.
     */
    @Override
    public String statement(Factor factor) {
        // IN 运算符：将集合展开为逗号分隔列表
        if (factor.operator() == Operator.IN) {
            if (factor.value() instanceof Collection<?> col) {
                StringBuilder sb = new StringBuilder(" IN (");
                List<String> items = new ArrayList<>();
                for (Object v : col) {
                    items.add(String.valueOf(normalizeValue(v, inferType(v))));
                }
                sb.append(String.join(",", items));
                sb.append(")");
                return factor.code() + sb;
            }
            // 单值 IN 回退为等值比较
            return factor.code() + " = " + normalizeValue(factor.value(), factor.valueType());
        }

        // BETWEEN：从双元素集合提取起止值
        if (factor.operator() == Operator.BETWEEN) {
            if (factor.value() instanceof Collection<?> col) {
                int i = 0;
                String start = null, end = null;
                for (Object v : col) {
                    if (i == 0) {
                        start = String.valueOf(normalizeValue(v, inferType(v)));
                    } else if (i == 1) {
                        end = String.valueOf(normalizeValue(v, inferType(v)));
                    }
                    i++;
                }
                return factor.code() + " BETWEEN " + start + " AND " + end;
            }
        }

        // IS NULL / IS NOT NULL 无右侧值
        if (factor.operator() == Operator.IS_NULL) {
            return factor.code() + " IS NULL";
        }
        if (factor.operator() == Operator.IS_NOT_NULL) {
            return factor.code() + " IS NOT NULL";
        }
        if (factor.operator() == Operator.HAS_VALUE) {
            return factor.code() + " IS NOT NULL";  // IS NOT NULL 的别名
        }

        // 默认：字段 运算符 值（如 age > 18）
        return factor.code() + " " + factor.operator().dbSymbol() + " "
                + normalizeValue(factor.value(), factor.valueType());
    }

    /**
     * 根据 Java 运行时类型推断 SQL 兼容的值类型。
     */
    private static FieldValueType inferType(Object value) {
        return switch (value) {
            case Integer i -> FieldValueType.INTEGER;
            case Long l -> FieldValueType.LONG;
            case Double v -> FieldValueType.DOUBLE;
            case Boolean b -> FieldValueType.BOOLEAN;
            case List<?> l -> FieldValueType.ARRAY;
            default -> FieldValueType.STRING;
        };
    }
}

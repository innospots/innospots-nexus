package com.innospots.nexus.base.domain.condition;

import com.innospots.nexus.base.domain.field.FieldValueType;

/**
 * 将 {@link Factor} 渲染为模式特定表达式字符串（SQL、脚本或 Java）的策略接口。
 * 实现负责值引用、类型转换与运算符特定格式化。
 *
 * @author Smars
 * @date 2026/09/13
 * @see DatabaseFactorStatement
 * @see ScriptFactorStatement
 * @see FactorStatementBuilder
 */
public interface IFactorStatement {

    /**
     * 将原始值规范化为模式安全的表示。
     *
     * @param value     原始值
     * @param valueType 值类型
     * @param operator  运算符
     * @return 规范化后的值
     */
    Object normalizeValue(Object value, FieldValueType valueType, Operator operator);

    /**
     * 将原始值规范化为模式安全的表示（无运算符）。
     *
     * @param value     原始值
     * @param valueType 值类型
     * @return 规范化后的值
     */
    default Object normalizeValue(Object value, FieldValueType valueType) {
        return normalizeValue(value, valueType, null);
    }

    /**
     * 将原始值规范化为字符串类型的模式安全表示。
     *
     * @param value 原始值
     * @return 规范化后的值
     */
    default Object normalizeValue(Object value) {
        return normalizeValue(value, FieldValueType.STRING, null);
    }

    /**
     * 从因子提取并规范化值。
     *
     * @param factor 过滤因子
     * @return 规范化后的值
     */
    Object normalizeValue(Factor factor);

    /**
     * 将单个因子渲染为完整表达式字符串。
     *
     * @param factor 过滤因子
     * @return 表达式字符串
     */
    String statement(Factor factor);
}

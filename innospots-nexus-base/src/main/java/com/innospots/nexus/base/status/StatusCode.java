package com.innospots.nexus.base.status;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 可组合状态码接口，由 3 字母模块码、{@link StatusCategory}（2 位数字）和 4 位本地码组成。
 * 完整码格式为 {@code module + category + localCode}。
 *
 * @author Smars
 * @date 2026/09/13
 * @see StatusCategory
 * @see NexusStatusCode
 * @see StatusCodeRules
 */
public interface StatusCode {

    /**
     * 返回模块码（3 位大写字母）。
     *
     * @return 模块码
     */
    String module();

    /**
     * 返回状态分类。
     *
     * @return 分类枚举
     */
    StatusCategory category();

    /**
     * 返回本地码（4 位数字）。
     *
     * @return 本地码
     */
    String localCode();

    /**
     * 返回国际化消息对象。
     *
     * @return 消息
     */
    I18nObject message();

    /**
     * 返回国际化建议对象。
     *
     * @return 建议
     */
    I18nObject advice();

    /**
     * 返回对应的 HTTP 状态码。
     *
     * @return HTTP 状态码
     */
    int httpStatusCode();

    /**
     * 返回枚举常量名称。
     *
     * @return 名称
     */
    String name();

    /**
     * 返回 BIS 完整状态码字符串。
     *
     * @return 完整状态码
     */
    default String bisCode() {
        StatusCodeRules.requireValid(module(), category(), localCode());
        return module() + category().code() + localCode();
    }

    /**
     * 返回完整状态码字符串（{@link #bisCode()} 的别名）。
     *
     * @return 完整状态码
     */
    default String fullCode() {
        return bisCode();
    }

    /**
     * 返回消息与建议的组合摘要。
     *
     * @return 摘要文本
     */
    default String summary() {
        String message = message() == null ? null : message().defaultValue();
        String advice = advice() == null ? null : advice().defaultValue();
        if (advice == null || advice.isBlank()) {
            return message;
        }
        return message + ", " + advice;
    }
}

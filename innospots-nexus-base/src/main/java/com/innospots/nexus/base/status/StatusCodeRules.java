package com.innospots.nexus.base.status;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.regex.Pattern;

/**
 * 状态码格式校验规则。
 * <ul>
 *   <li>模块码：恰好 3 个大写字母（如 {@code AIO}）</li>
 *   <li>分类：非 null 的 {@link StatusCategory}</li>
 *   <li>本地码：恰好 4 位数字（如 {@code 0001}）</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see StatusCode
 * @see StatusCategory
 */
public final class StatusCodeRules {

    private static final Pattern MODULE_PATTERN = Pattern.compile("[A-Z]{3}");
    private static final Pattern LOCAL_CODE_PATTERN = Pattern.compile("\\d{4}");

    private StatusCodeRules() {
    }

    /**
     * 校验状态码各组成部分的格式合法性。
     *
     * @param module    模块码
     * @param category  状态分类
     * @param localCode 本地码
     * @throws IllegalArgumentException 格式不合法时
     */
    public static void requireValid(String module, StatusCategory category, String localCode) {
        if (!MODULE_PATTERN.matcher(CharSequenceUtil.nullToEmpty(module)).matches()) {
            throw new IllegalArgumentException("Status module code must be three uppercase letters");
        }
        if (category == null) {
            throw new IllegalArgumentException("Status category must not be null");
        }
        if (!LOCAL_CODE_PATTERN.matcher(CharSequenceUtil.nullToEmpty(localCode)).matches()) {
            throw new IllegalArgumentException("Status local code must be four digits");
        }
    }
}

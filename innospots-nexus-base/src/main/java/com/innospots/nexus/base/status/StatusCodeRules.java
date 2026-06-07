package com.innospots.nexus.base.status;

import cn.hutool.core.text.CharSequenceUtil;

import java.util.regex.Pattern;

/**
 * Validation rules for status code formatting.
 * <ul>
 *   <li>Module: exactly 3 uppercase letters (e.g. {@code NEX})</li>
 *   <li>Category: non-null {@link StatusCategory}</li>
 *   <li>Local code: exactly 4 digits (e.g. {@code 0001})</li>
 * </ul>
 */
public final class StatusCodeRules {

    private static final Pattern MODULE_PATTERN = Pattern.compile("[A-Z]{3}");
    private static final Pattern LOCAL_CODE_PATTERN = Pattern.compile("\\d{4}");

    private StatusCodeRules() {
    }

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

package com.innospots.nexus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String utilities: blank checks, placeholder replacement
 * ({@code ${key}} and {@code {{key}}}), camelCase/underscore
 * conversion, and random key generation.
 */
public final class StringUtils {

    private static final String KEY_SEED = "123456789abcdefghijklmnopqrstuvwxyz";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}|\\{\\{([^}]+)}}");

    private StringUtils() {
    }

    /** Checks if a CharSequence is null or blank. */
    public static boolean isBlank(CharSequence value) {
        return CharSequenceUtil.isBlank(value);
    }

    /** Returns the value if non-blank, otherwise the default. */
    public static String defaultIfBlank(String value, String defaultValue) {
        return CharSequenceUtil.blankToDefault(value, defaultValue);
    }

    /**
     * Replaces placeholders in a string using the provided value map.
     * Supports both {@code ${key}} and {@code {{key}}} syntax.
     * Unmatched placeholders are left unchanged.
     */
    public static String replacePlaceholders(String text, Map<String, ?> values) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (values == null || values.isEmpty()) {
            return text;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = placeholderKey(matcher);
            if (values.containsKey(key)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(converter(values.get(key))));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Converts camelCase to underscore_case. */
    public static String camelToUnderscore(String camelValue) {
        return CharSequenceUtil.toUnderlineCase(camelValue);
    }

    /** Converts underscore_case to camelCase. */
    public static String underscoreToCamel(String underscoreValue) {
        return CharSequenceUtil.toCamelCase(underscoreValue);
    }

    /** Generates a random alphanumeric key of the specified length. */
    public static String randomKey(int count) {
        return RandomUtil.randomString(KEY_SEED, count);
    }

    public static String converter(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    private static String placeholderKey(Matcher matcher) {
        String key = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
        return key.trim();
    }
}

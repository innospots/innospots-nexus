package com.innospots.nexus.base.util;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类，提供空白判断、占位符替换（{@code ${key}} 与 {@code {{key}}}）、
 * 驼峰/下划线命名转换以及随机键生成等能力。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class StringUtils {

    private static final String KEY_SEED = "123456789abcdefghijklmnopqrstuvwxyz";
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)}|\\{\\{([^}]+)}}");

    private StringUtils() {
    }

    /**
     * 判断字符序列是否为 null 或空白。
     *
     * @param value 待检查的字符序列
     * @return 为 null 或空白时返回 {@code true}
     */
    public static boolean isBlank(CharSequence value) {
        return CharSequenceUtil.isBlank(value);
    }

    /**
     * 判断字符序列是否非 null 且非空白。
     *
     * @param value 待检查的字符序列
     * @return 非 null 且非空白时返回 {@code true}
     */
    public static boolean isNotBlank(CharSequence value) {
        return CharSequenceUtil.isNotBlank(value);
    }

    /**
     * 判断字符序列是否为 null 或空串。
     *
     * @param value 待检查的字符序列
     * @return 为 null 或空串时返回 {@code true}
     */
    public static boolean isEmpty(CharSequence value) {
        return CharSequenceUtil.isEmpty(value);
    }

    /**
     * 判断字符序列是否非 null 且非空串。
     *
     * @param value 待检查的字符序列
     * @return 非 null 且非空串时返回 {@code true}
     */
    public static boolean isNotEmpty(CharSequence value) {
        return CharSequenceUtil.isNotEmpty(value);
    }

    /**
     * 非空白时返回原值，否则返回默认值。
     *
     * @param value        原始字符串
     * @param defaultValue 空白时的回退值
     * @return 解析后的字符串
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return CharSequenceUtil.blankToDefault(value, defaultValue);
    }

    /**
     * 使用给定值映射替换字符串中的占位符。
     * 支持 {@code ${key}} 与 {@code {{key}}} 两种语法；未匹配的占位符保持原样。
     *
     * @param text   含占位符的模板文本
     * @param values 占位符键值映射
     * @return 替换后的字符串
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

    /**
     * 将 camelCase 转换为 underscore_case。
     *
     * @param camelValue 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String camelToUnderscore(String camelValue) {
        return CharSequenceUtil.toUnderlineCase(camelValue);
    }

    /**
     * 将 underscore_case 转换为 camelCase。
     *
     * @param underscoreValue 下划线命名字符串
     * @return 驼峰命名字符串
     */
    public static String underscoreToCamel(String underscoreValue) {
        return CharSequenceUtil.toCamelCase(underscoreValue);
    }

    /**
     * 生成指定长度的随机字母数字键。
     *
     * @param count 键长度
     * @return 随机键字符串
     */
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

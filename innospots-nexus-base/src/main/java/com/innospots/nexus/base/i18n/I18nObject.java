package com.innospots.nexus.base.i18n;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.innospots.nexus.base.json.I18nObjectDeserializer;
import com.innospots.nexus.base.json.I18nObjectSerializer;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 表示国际化字符串的语言环境到值的映射。
 * 提供带回退的语言环境感知查找：精确匹配（如 {@code zh-CN}）→ 仅语言（{@code zh}）→ 首个可用值。
 *
 * @author Smars
 * @date 2026/09/13
 * @see I18nConverter
 * @see I18nObjectSerializer
 */
@JsonSerialize(using = I18nObjectSerializer.class)
@JsonDeserialize(using = I18nObjectDeserializer.class)
public class I18nObject extends LinkedHashMap<String, String> {

    private static final String DEFAULT_LANGUAGE = Locale.US.getLanguage();

    /**
     * 创建仅包含默认语言（en）单个值的 I18nObject。
     *
     * @param value 默认值
     * @return I18nObject 实例
     */
    public static I18nObject of(String value) {
        I18nObject object = new I18nObject();
        object.put(DEFAULT_LANGUAGE, value);
        return object;
    }

    /**
     * 创建包含单个语言环境-值对的 I18nObject。
     *
     * @param locale 语言环境
     * @param value  值
     * @return I18nObject 实例
     */
    public static I18nObject of(String locale, String value) {
        I18nObject object = new I18nObject();
        object.put(locale, value);
        return object;
    }

    /**
     * 从现有语言环境-值映射创建 I18nObject。
     *
     * @param values 源映射
     * @return I18nObject 实例
     */
    public static I18nObject of(Map<String, String> values) {
        I18nObject object = new I18nObject();
        if (values != null) {
            object.putAll(values);
        }
        return object;
    }

    /**
     * 从语言环境-值对创建 I18nObject。
     * 示例：{@code I18nObject.of("en", "Hello", "zh", "你好")}
     *
     * @param pairs 交替的语言环境、值、语言环境、值……
     * @return I18nObject 实例
     * @throws IllegalArgumentException 参数个数为奇数时
     */
    public static I18nObject of(String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Locale/value pairs must be even");
        }
        I18nObject object = new I18nObject();
        for (int i = 0; i < pairs.length; i += 2) {
            object.put(pairs[i], pairs[i + 1]);
        }
        return object;
    }

    /**
     * 返回当前线程语言环境对应的值。
     *
     * @return 本地化值
     */
    public String defaultValue() {
        return value(I18nConverter.locale());
    }

    /**
     * 返回英语（US）值。
     *
     * @return 英文值
     */
    public String enValue() {
        return value(Locale.US);
    }

    /**
     * 返回简体中文值。
     *
     * @return 中文值
     */
    public String cnValue() {
        return value(Locale.SIMPLIFIED_CHINESE);
    }

    /**
     * 按以下回退顺序解析给定语言环境的值：
     * 精确匹配（如 zh-CN）→ 仅语言（zh）→ 同语言组的其他变体 → 首个可用值。
     *
     * @param locale 目标语言环境
     * @return 解析后的值；映射为空时返回 null
     */
    public String value(Locale locale) {
        if (isEmpty()) {
            return null;
        }
        Locale targetLocale = locale == null ? Locale.getDefault() : locale;
        // 优先精确匹配：language-COUNTRY（如 "zh-CN"）
        String value = get(normalizedLocale(targetLocale));
        if (value == null) {
            // 回退到仅语言键（如 "zh"）
            value = get(targetLocale.getLanguage());
        }
        // 目标为中文时回退到任意中文变体
        if (value == null && I18nConverter.isChineseLocale(targetLocale)) {
            value = firstValue(I18nConverter.ZH_LOCALES);
        }
        // 目标为英文时回退到任意英文变体
        if (value == null && I18nConverter.isEnglishLocale(targetLocale)) {
            value = firstValue(I18nConverter.EN_LOCALES);
        }
        // 最终回退：映射中的第一个条目
        return value == null ? firstValue() : value;
    }

    private String firstValue(Set<String> locales) {
        for (String locale : locales) {
            String value = get(locale);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstValue() {
        Map.Entry<String, String> entry = entrySet().stream().findFirst().orElse(null);
        return entry == null ? null : entry.getValue();
    }

    private static String normalizedLocale(Locale locale) {
        if (locale.getCountry() == null || locale.getCountry().isBlank()) {
            return locale.getLanguage();
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }
}

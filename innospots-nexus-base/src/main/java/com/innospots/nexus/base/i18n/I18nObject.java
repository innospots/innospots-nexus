package com.innospots.nexus.base.i18n;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A locale-to-value map representing an internationalized string.
 * Provides locale-aware lookup with fallback: exact match (e.g.
 * {@code zh-CN}) → language-only ({@code zh}) → first available value.
 */
public class I18nObject extends LinkedHashMap<String, String> {

    private static final String DEFAULT_LANGUAGE = Locale.US.getLanguage();

    /**
     * Creates an I18nObject with a single value for the default language (en).
     */
    public static I18nObject of(String value) {
        I18nObject object = new I18nObject();
        object.put(DEFAULT_LANGUAGE, value);
        return object;
    }

    /**
     * Creates an I18nObject with a single locale-value pair.
     */
    public static I18nObject of(String locale, String value) {
        I18nObject object = new I18nObject();
        object.put(locale, value);
        return object;
    }

    /**
     * Creates an I18nObject from an existing locale-value map.
     */
    public static I18nObject of(Map<String, String> values) {
        I18nObject object = new I18nObject();
        if (values != null) {
            object.putAll(values);
        }
        return object;
    }

    /**
     * Creates an I18nObject from locale-value pairs.
     * Example: {@code I18nObject.of("en", "Hello", "zh", "你好")}
     *
     * @param pairs alternating locale, value, locale, value, ...
     * @throws IllegalArgumentException if the number of arguments is odd
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
     * Returns the value for the current thread locale.
     */
    public String defaultValue() {
        return value(I18nConverter.locale());
    }

    /** Returns the English (US) value. */
    public String enValue() {
        return value(Locale.US);
    }

    /** Returns the Simplified Chinese value. */
    public String cnValue() {
        return value(Locale.SIMPLIFIED_CHINESE);
    }

    /**
     * Resolves the value for the given locale with the following fallback:
     * exact match (e.g. zh-CN) -> language-only (zh) -> other locale variants
     * in the same language group -> first available value.
     */
    public String value(Locale locale) {
        if (isEmpty()) {
            return null;
        }
        Locale targetLocale = locale == null ? Locale.getDefault() : locale;
        // Try exact match first: language-COUNTRY (e.g. "zh-CN")
        String value = get(normalizedLocale(targetLocale));
        if (value == null) {
            // Fall back to language-only key (e.g. "zh")
            value = get(targetLocale.getLanguage());
        }
        // Fall back to any Chinese variant if target is Chinese
        if (value == null && I18nConverter.isChineseLocale(targetLocale)) {
            value = firstValue(I18nConverter.ZH_LOCALES);
        }
        // Fall back to any English variant if target is English
        if (value == null && I18nConverter.isEnglishLocale(targetLocale)) {
            value = firstValue(I18nConverter.EN_LOCALES);
        }
        // Ultimate fallback: first entry in the map
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

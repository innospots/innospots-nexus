package com.innospots.nexus.base.i18n;

import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Core i18n translation engine. Supports two translation modes:
 * <ol>
 *   <li><b>I18n key resolution</b> — strings wrapped in {@code ${key}} are
 *       resolved via an {@link I18nMessageResolver}.</li>
 *   <li><b>I18nObject translation</b> — {@link I18nObject} instances return
 *       the value for the current thread-local {@link Locale}.</li>
 * </ol>
 * <p>The current locale is stored in a {@link ThreadLocal} and can be set
 * via {@link #setLocale(Locale)}.</p>
 */
public final class I18nConverter {

    public static final Set<String> ZH_LOCALES = new LinkedHashSet<>(List.of(
            "zh", "zh-CN", "zh_CN", "zh-Hans", "zh_Hans", "zh-TW", "zh_TW", "zh-HK", "zh_HK"
    ));
    public static final Set<String> EN_LOCALES = new LinkedHashSet<>(List.of(
            "en", "en-US", "en_US", "en-GB", "en_GB", "en-CA", "en_CA", "en-AU", "en_AU"
    ));

    private static final String PREFIX = "${";
    private static final String SUFFIX = "}";
    private static final String DEFAULT_SEPARATOR = "|#|";
    private static final ThreadLocal<Locale> LOCALE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IGNORE_I18N = ThreadLocal.withInitial(() -> false);
    private static final AtomicReference<I18nMessageResolver> MESSAGE_RESOLVER = new AtomicReference<>();

    private I18nConverter() {
    }

    /**
     * Sets the current thread's locale for i18n resolution.
     *
     * @param locale the locale, or null to clear (fallback to system default)
     */
    public static void setLocale(Locale locale) {
        if (locale == null) {
            LOCALE.remove();
        } else {
            LOCALE.set(locale);
        }
    }

    /**
     * Returns the current thread's locale, falling back to the JVM default.
     */
    public static Locale locale() {
        Locale locale = LOCALE.get();
        return locale == null ? Locale.getDefault() : locale;
    }

    /**
     * Registers a global message resolver. This is an atomic reference,
     * so it can be set once at application startup.
     */
    public static void setMessageResolver(I18nMessageResolver resolver) {
        MESSAGE_RESOLVER.set(resolver);
    }

    /** Globally disables i18n translation for the current thread. */
    public static void ignoreI18n() {
        IGNORE_I18N.set(true);
    }

    /** Returns whether i18n translation is globally disabled for this thread. */
    public static boolean shouldIgnoreI18n() {
        return IGNORE_I18N.get();
    }

    /** Clears all thread-local state and the message resolver. */
    public static void clear() {
        LOCALE.remove();
        IGNORE_I18N.remove();
        MESSAGE_RESOLVER.set(null);
    }

    /**
     * Checks if a string value is an i18n key expression (wrapped in {@code ${...}}).
     */
    public static boolean isI18nField(String value) {
        return value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX);
    }

    /**
     * Extracts the i18n key name from a wrapped expression.
     * For {@code "${user.name}"}, returns {@code "user.name"}.
     */
    public static String i18nName(String value) {
        return isI18nField(value) ? value.substring(PREFIX.length(), value.length() - SUFFIX.length()) : null;
    }

    /**
     * Wraps a key string in the i18n prefix/suffix: {@code key -> "${key}"}.
     */
    public static String wrapKey(String key) {
        return PREFIX + key + SUFFIX;
    }

    /**
     * Recursively translates an object:
     * <ul>
     *   <li>{@link I18nObject} — lookup by current locale</li>
     *   <li>{@link String} — resolve if it is an i18n key expression</li>
     *   <li>{@link Map}/{@link List} — recursively translate entries</li>
     *   <li>Other types — returned unchanged</li>
     * </ul>
     */
    public static Object translate(Object value) {
        if (value == null || shouldIgnoreI18n()) {
            return value;
        }
        if (value instanceof I18nObject object) {
            return object.value(locale());
        }
        if (value instanceof String text) {
            return translateString(text);
        }
        if (value instanceof Map<?, ?> map) {
            return translateMap(map);
        }
        if (value instanceof List<?> list) {
            return translateList(list);
        }
        return value;
    }

    /**
     * Translates a single string value. If it is an i18n key ({@code ${key}}),
     * resolves it via the message resolver. Supports inline default values
     * with the syntax {@code ${key:default}} and locale-paired defaults
     * separated by {@code |#|}.
     */
    public static String translateString(String value) {
        if (StrUtil.isBlank(value) || shouldIgnoreI18n() || !isI18nField(value)) {
            return value;
        }
        I18nToken token = I18nToken.parse(i18nName(value));
        String message = resolve(token.key());
        if (StrUtil.isNotBlank(message) && !message.equals(token.key())) {
            return message;
        }
        // Fall back to inline default value with locale awareness
        String defaultValue = defaultValue(token.defaultValue(), locale());
        return StrUtil.isBlank(defaultValue) ? token.key() : defaultValue;
    }

    public static boolean isChineseLocale(Locale locale) {
        return locale != null && (Locale.CHINESE.equals(locale)
                || Locale.SIMPLIFIED_CHINESE.equals(locale)
                || Locale.TRADITIONAL_CHINESE.equals(locale)
                || ZH_LOCALES.contains(locale.getLanguage())
                || ZH_LOCALES.contains(locale.toLanguageTag())
                || ZH_LOCALES.contains(locale.toString()));
    }

    public static boolean isEnglishLocale(Locale locale) {
        return locale != null && (Locale.ENGLISH.equals(locale)
                || Locale.US.equals(locale)
                || Locale.UK.equals(locale)
                || EN_LOCALES.contains(locale.getLanguage())
                || EN_LOCALES.contains(locale.toLanguageTag())
                || EN_LOCALES.contains(locale.toString()));
    }

    private static Map<String, Object> translateMap(Map<?, ?> source) {
        Map<String, Object> translated = new LinkedHashMap<>();
        source.forEach((key, value) -> translated.put(String.valueOf(key), translate(value)));
        return translated;
    }

    private static List<Object> translateList(List<?> source) {
        List<Object> translated = new ArrayList<>(source.size());
        source.forEach(item -> translated.add(translate(item)));
        return translated;
    }

    private static String resolve(String key) {
        I18nMessageResolver resolver = MESSAGE_RESOLVER.get();
        return resolver == null ? null : resolver.resolve(key, locale());
    }

    private static String defaultValue(String defaultValue, Locale locale) {
        if (defaultValue == null || !defaultValue.contains(DEFAULT_SEPARATOR)) {
            return defaultValue;
        }
        String[] values = defaultValue.split("\\Q" + DEFAULT_SEPARATOR + "\\E", 2);
        if (values.length < 2) {
            return defaultValue;
        }
        return isChineseLocale(locale) ? values[0] : values[1];
    }

    private record I18nToken(String key, String defaultValue) {

        private static I18nToken parse(String value) {
            int index = value == null ? -1 : value.indexOf(':');
            if (index < 1) {
                return new I18nToken(value, null);
            }
            return new I18nToken(value.substring(0, index), value.substring(index + 1));
        }
    }
}

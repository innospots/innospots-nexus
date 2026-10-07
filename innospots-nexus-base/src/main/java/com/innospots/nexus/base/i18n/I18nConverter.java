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
import java.util.regex.Pattern;

/**
 * 核心 i18n 翻译引擎。支持两种翻译模式：
 * <ol>
 *   <li><b>i18n 键解析</b> — 包裹在 {@code ${key}} 中的字符串通过 {@link I18nMessageResolver} 解析。</li>
 *   <li><b>I18nObject 翻译</b> — {@link I18nObject} 实例返回当前线程本地 {@link Locale} 对应的值。</li>
 * </ol>
 * <p>当前语言环境存储在 {@link ThreadLocal} 中，可通过 {@link #setLocale(Locale)} 设置。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see I18nObject
 * @see I18nMessageResolver
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
    private static final Pattern LOCALE_KEY_PATTERN = Pattern.compile(
            "^[a-zA-Z]{2,3}(?:[-_][a-zA-Z0-9]{2,8})*$"
    );
    private static final ThreadLocal<Locale> LOCALE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IGNORE_I18N = ThreadLocal.withInitial(() -> false);
    private static final AtomicReference<I18nMessageResolver> MESSAGE_RESOLVER = new AtomicReference<>();

    private I18nConverter() {
    }

    /**
     * 设置当前线程的 i18n 解析语言环境。
     *
     * @param locale 语言环境；为 null 时清除（回退到系统默认）
     */
    public static void setLocale(Locale locale) {
        if (locale == null) {
            LOCALE.remove();
        } else {
            LOCALE.set(locale);
        }
    }

    /**
     * 返回当前线程的语言环境，回退到 JVM 默认值。
     *
     * @return 当前语言环境
     */
    public static Locale locale() {
        Locale locale = LOCALE.get();
        return locale == null ? Locale.getDefault() : locale;
    }

    /**
     * 注册全局消息解析器。使用原子引用，可在应用启动时设置一次。
     *
     * @param resolver 消息解析器
     */
    public static void setMessageResolver(I18nMessageResolver resolver) {
        MESSAGE_RESOLVER.set(resolver);
    }

    /**
     * 全局禁用当前线程的 i18n 翻译。
     */
    public static void ignoreI18n() {
        IGNORE_I18N.set(true);
    }

    /**
     * 返回当前线程是否全局禁用了 i18n 翻译。
     *
     * @return 禁用时返回 {@code true}
     */
    public static boolean shouldIgnoreI18n() {
        return IGNORE_I18N.get();
    }

    /**
     * 清除所有线程本地状态及消息解析器。
     */
    public static void clear() {
        LOCALE.remove();
        IGNORE_I18N.remove();
        MESSAGE_RESOLVER.set(null);
    }

    /**
     * 判断字符串是否为 i18n 键表达式（包裹在 {@code ${...}} 中）。
     *
     * @param value 待检查的字符串
     * @return 是 i18n 键时返回 {@code true}
     */
    public static boolean isI18nField(String value) {
        return value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX);
    }

    /**
     * 判断类型是否为 {@link I18nObject} 或其子类。
     *
     * @param type 待检查类型
     * @return 是 I18nObject 类型时返回 {@code true}
     */
    public static boolean isI18nObjectType(Class<?> type) {
        return type != null && I18nObject.class.isAssignableFrom(type);
    }

    /**
     * 判断运行时值是否可解析为 {@link I18nObject}：
     * {@link I18nObject} 实例、非空字符串，或键均为语言标签且值为字符串的 Map。
     *
     * @param value 待检查值
     * @return 符合 I18nObject 形态时返回 {@code true}
     */
    public static boolean isI18nObjectShape(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof I18nObject) {
            return true;
        }
        if (value instanceof String text) {
            return !text.isBlank();
        }
        if (!(value instanceof Map<?, ?> map)) {
            return false;
        }
        if (map.isEmpty()) {
            return true;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                return false;
            }
            if (!(entry.getValue() instanceof String)) {
                return false;
            }
            if (!isLocaleKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将字符串、Map 或已有 {@link I18nObject} 规范化为 {@link I18nObject}。
     *
     * @param value 源值
     * @return 解析结果；源值为 null 时返回 null
     * @throws IllegalArgumentException 值不符合 I18nObject 形态时
     */
    public static I18nObject parseI18nObject(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof I18nObject object) {
            return object;
        }
        if (value instanceof String text) {
            if (text.isBlank()) {
                return I18nObject.of(Map.of());
            }
            return I18nObject.of(text.trim());
        }
        if (value instanceof Map<?, ?> map) {
            if (!isI18nObjectShape(map)) {
                throw new IllegalArgumentException("Value is not an I18nObject shape");
            }
            Map<String, String> locales = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                locales.put(String.valueOf(entry.getKey()), (String) entry.getValue());
            }
            return I18nObject.of(locales);
        }
        throw new IllegalArgumentException("Unsupported I18nObject source type: " + value.getClass().getName());
    }

    /**
     * 从包裹表达式中提取 i18n 键名。
     * 对 {@code "${user.name}"} 返回 {@code "user.name"}。
     *
     * @param value 包裹表达式
     * @return 键名；非 i18n 字段时返回 null
     */
    public static String i18nName(String value) {
        return isI18nField(value) ? value.substring(PREFIX.length(), value.length() - SUFFIX.length()) : null;
    }

    /**
     * 用 i18n 前缀/后缀包裹键字符串：{@code key -> "${key}"}。
     *
     * @param key 原始键
     * @return 包裹后的表达式
     */
    public static String wrapKey(String key) {
        return PREFIX + key + SUFFIX;
    }

    /**
     * 按 {@link I18n} 注解与字段值翻译。
     * <p>
     * 当 {@code annotationKey} 非空时，优先将其视为 i18n 键（可写 {@code ${key}} 或裸键 {@code key}）；
     * 否则对 {@code fieldValue} 执行 {@link #translate(Object)}。
     *
     * @param annotationKey {@link I18n#value()}；为空时忽略
     * @param fieldValue    成员变量当前值
     * @return 翻译后的 JSON 可写入值
     */
    public static Object translateAnnotatedField(String annotationKey, Object fieldValue) {
        if (shouldIgnoreI18n()) {
            return fieldValue;
        }
        if (StrUtil.isNotBlank(annotationKey)) {
            return translateString(toKeyExpression(annotationKey));
        }
        return translate(fieldValue);
    }

    /**
     * 将裸 i18n 键或已包裹的 {@code ${key}} 表达式规范化为 {@code ${key}} 形式。
     *
     * @param key 裸键或包裹表达式
     * @return 规范表达式
     */
    public static String toKeyExpression(String key) {
        if (key == null) {
            return null;
        }
        String trimmed = key.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (isI18nField(trimmed)) {
            return trimmed;
        }
        return wrapKey(trimmed);
    }

    /**
     * 递归翻译对象：
     * <ul>
     *   <li>{@link I18nObject} — 按当前语言环境查找</li>
     *   <li>{@link String} — 若为 i18n 键表达式则解析</li>
     *   <li>{@link Map}/{@link List} — 递归翻译条目</li>
     *   <li>其他类型 — 原样返回</li>
     * </ul>
     *
     * @param value 待翻译的值
     * @return 翻译后的值
     */
    public static Object translate(Object value) {
        if (value == null || shouldIgnoreI18n()) {
            return value;
        }
        if (value instanceof I18nObject object) {
            return object.value(locale());
        }
        if (value instanceof Map<?, ?> map && isI18nObjectShape(map)) {
            return parseI18nObject(map).value(locale());
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
     * 翻译单个字符串。若为 i18n 键（{@code ${key}}），通过消息解析器解析。
     * 支持内联默认值语法 {@code ${key:default}} 及以 {@code |#|} 分隔的语言环境配对默认值。
     *
     * @param value 待翻译的字符串
     * @return 翻译后的字符串
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
        // 回退到带语言环境感知的内联默认值
        String defaultValue = defaultValue(token.defaultValue(), locale());
        return StrUtil.isBlank(defaultValue) ? token.key() : defaultValue;
    }

    /**
     * 判断给定语言环境是否为中文变体。
     *
     * @param locale 语言环境
     * @return 是中文时返回 {@code true}
     */
    public static boolean isChineseLocale(Locale locale) {
        return locale != null && (Locale.CHINESE.equals(locale)
                || Locale.SIMPLIFIED_CHINESE.equals(locale)
                || Locale.TRADITIONAL_CHINESE.equals(locale)
                || ZH_LOCALES.contains(locale.getLanguage())
                || ZH_LOCALES.contains(locale.toLanguageTag())
                || ZH_LOCALES.contains(locale.toString()));
    }

    /**
     * 判断给定语言环境是否为英文变体。
     *
     * @param locale 语言环境
     * @return 是英文时返回 {@code true}
     */
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

    private static boolean isLocaleKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (ZH_LOCALES.contains(key) || EN_LOCALES.contains(key)) {
            return true;
        }
        if (!LOCALE_KEY_PATTERN.matcher(key).matches()) {
            return false;
        }
        String tag = key.replace('_', '-');
        Locale parsed = Locale.forLanguageTag(tag);
        return !parsed.getLanguage().isEmpty() && !"und".equals(parsed.getLanguage());
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

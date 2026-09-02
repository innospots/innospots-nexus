package com.innospots.nexus.console.plugin.contribution;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** Console 静态声明共用的本地化文本校验与防御性复制工具。 */
final class ConsoleI18n {

    private static final Pattern LANGUAGE_TAG_PATTERN = Pattern.compile(
            "[A-Za-z]{2,8}(?:-[A-Za-z0-9]{1,8})*");

    private ConsoleI18n() {
    }

    /** 复制并校验一个本地化文本对象。 */
    static I18nObject copy(
            I18nObject source,
            boolean required,
            String field,
            PluginStatusCode statusCode
    ) {
        if (source == null) {
            if (required) {
                invalid(field + " must not be empty", statusCode);
            }
            return I18nObject.of(Map.of());
        }
        if (required && source.isEmpty()) {
            invalid(field + " must not be empty", statusCode);
        }
        if (!required && source.isEmpty()) {
            return I18nObject.of(Map.of());
        }
        return source == null
                ? I18nObject.of(Map.of())
                : from(new LinkedHashMap<>(source), field, statusCode);
    }

    /** 校验并规范化 manifest 解码得到的本地化映射。 */
    static I18nObject from(Map<String, String> source, String field, PluginStatusCode statusCode) {
        if (source == null || source.isEmpty()) {
            invalid(field + " must not be empty", statusCode);
        }
        if (source.size() > 32) {
            invalid(field + " contains too many languages", statusCode);
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            String languageTag = entry.getKey();
            String value = entry.getValue();
            if (languageTag == null || !LANGUAGE_TAG_PATTERN.matcher(languageTag).matches()
                    || value == null || value.isBlank() || value.length() > 256) {
                invalid(field + " contains an invalid localized value", statusCode);
            }
            String canonicalTag = Locale.forLanguageTag(languageTag).toLanguageTag();
            if (canonicalTag.isBlank() || ("und".equals(canonicalTag)
                    && !"und".equalsIgnoreCase(languageTag))) {
                invalid(field + " contains an invalid language tag: " + languageTag, statusCode);
            }
            if (normalized.putIfAbsent(canonicalTag, value) != null) {
                invalid(field + " contains duplicate language tag: " + canonicalTag, statusCode);
            }
        }
        return I18nObject.of(normalized);
    }

    private static void invalid(String message, PluginStatusCode statusCode) {
        throw NexusException.build(statusCode, message);
    }
}

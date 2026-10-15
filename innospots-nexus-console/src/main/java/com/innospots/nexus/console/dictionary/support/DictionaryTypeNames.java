package com.innospots.nexus.console.dictionary.support;

import java.util.Locale;
import java.util.Map;

import com.innospots.nexus.base.i18n.I18nObject;
import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.dictionary.domain.entity.DictionaryTypeEntity;
import com.innospots.nexus.console.support.ConsoleExceptions;

/**
 * 字典类型名称国际化读写：JSON 存 {@code type_name_i18n}，{@code type_name} 存简体中文摘要供检索与排序。
 */
public final class DictionaryTypeNames {

    public static final String LOCALE_ZH_CN = "zh-CN";

    private static final int TYPE_NAME_MAX_LENGTH = 128;

    private DictionaryTypeNames() {
    }

    /**
     * 将请求中的多语言名称写入实体。
     *
     * @param entity   目标实体
     * @param typeName 多语言名称
     */
    public static void apply(DictionaryTypeEntity entity, I18nObject typeName) {
        if (typeName == null || typeName.isEmpty()) {
            throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
        }
        I18nObject normalized = normalize(typeName);
        String primary = normalized.value(Locale.SIMPLIFIED_CHINESE);
        if (primary == null || primary.isBlank()) {
            throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER);
        }
        entity.setTypeName(truncate(primary.trim()));
        entity.setTypeNameI18n(Jsons.toJson(new java.util.LinkedHashMap<>(normalized)));
    }

    /**
     * 从实体读取完整多语言名称；无 JSON 列时按历史单语言字段回退为 zh-CN。
     *
     * @param entity 字典类型实体
     * @return 多语言名称
     */
    public static I18nObject read(DictionaryTypeEntity entity) {
        String raw = entity.getTypeNameI18n();
        if (raw != null && !raw.isBlank()) {
            I18nObject parsed = Jsons.fromJson(raw, I18nObject.class);
            if (parsed != null && !parsed.isEmpty()) {
                return parsed;
            }
        }
        String legacy = entity.getTypeName();
        if (legacy == null || legacy.isBlank()) {
            return new I18nObject();
        }
        return I18nObject.of(LOCALE_ZH_CN, legacy);
    }

    /**
     * 列表与选项展示用的主标签（优先简体中文）。
     *
     * @param entity 字典类型实体
     * @return 展示文案
     */
    /**
     * 将补丁合并进已有多语言名称（更新时保留未提交的语言项）。
     *
     * @param existing 当前存储的名称
     * @param patch    请求中的名称补丁
     * @return 合并后的名称
     */
    public static I18nObject merge(I18nObject existing, I18nObject patch) {
        I18nObject merged = new I18nObject();
        if (existing != null) {
            merged.putAll(existing);
        }
        if (patch != null) {
            merged.putAll(patch);
        }
        return merged;
    }

    public static String displayLabel(DictionaryTypeEntity entity) {
        I18nObject names = read(entity);
        String label = names.value(Locale.SIMPLIFIED_CHINESE);
        if (label == null || label.isBlank()) {
            label = names.defaultValue();
        }
        return label == null ? "" : label;
    }

    private static I18nObject normalize(I18nObject source) {
        I18nObject normalized = new I18nObject();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                continue;
            }
            String value = entry.getValue();
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            normalized.put(entry.getKey().trim(), trimmed);
        }
        return normalized;
    }

    private static String truncate(String value) {
        if (value.length() <= TYPE_NAME_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, TYPE_NAME_MAX_LENGTH);
    }
}

package com.innospots.nexus.base.domain.dictionary;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 字典类型内的单个键值条目。显示名称与类型名称均支持国际化（{@link I18nObject}）。
 *
 * @author Smars
 * @date 2026/09/13
 * @param value    字典值
 * @param name     国际化显示名称
 * @param type     字典类型编码
 * @param typeName 国际化类型名称
 * @param status   状态
 * @see DictionaryType
 */
public record DictionaryItem(
        String value,
        I18nObject name,
        String type,
        I18nObject typeName,
        BasicStatus status
) {
}

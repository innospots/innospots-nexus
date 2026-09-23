package com.innospots.nexus.base.domain.dictionary;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * 字典类型（如 "gender"、"country"），用于分组相关的 {@link DictionaryItem} 条目。
 * 显示名称支持国际化。
 *
 * @author Smars
 * @date 2026/09/13
 * @param code   字典类型编码
 * @param name   国际化显示名称
 * @param status 状态
 * @see DictionaryItem
 */
public record DictionaryType(
        String code,
        I18nObject name,
        BasicStatus status
) {
}

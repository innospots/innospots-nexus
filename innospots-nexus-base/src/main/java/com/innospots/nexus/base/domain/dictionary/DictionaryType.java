package com.innospots.nexus.base.domain.dictionary;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * A dictionary type (e.g. "gender", "country") that groups related
 * {@link DictionaryItem} entries. The display name is internationalized.
 */
public record DictionaryType(
        String code,
        I18nObject name,
        BasicStatus status
) {
}

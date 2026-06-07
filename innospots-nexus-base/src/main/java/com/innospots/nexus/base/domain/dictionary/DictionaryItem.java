package com.innospots.nexus.base.domain.dictionary;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.i18n.I18nObject;

/**
 * A single key-value entry within a dictionary type. The display name
 * and type name are internationalized ({@link I18nObject}).
 */
public record DictionaryItem(
        String value,
        I18nObject name,
        String type,
        I18nObject typeName,
        BasicStatus status
) {
}

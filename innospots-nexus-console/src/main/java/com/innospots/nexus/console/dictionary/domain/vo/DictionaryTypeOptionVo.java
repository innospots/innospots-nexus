package com.innospots.nexus.console.dictionary.domain.vo;

/**
 * Compact dictionary type option for selectors.
 *
 * @param dictionaryTypeId type identifier
 * @param typeCode         stable type code
 * @param typeName         display name
 */
public record DictionaryTypeOptionVo(
        String dictionaryTypeId,
        String typeCode,
        String typeName
) {
}

package com.innospots.nexus.console.dictionary.domain.request;

/**
 * Request for updating mutable dictionary type fields. The type code is immutable.
 *
 * @param typeName  display name
 * @param sortOrder display order
 */
public record DictionaryTypeUpdateRequest(
        String typeName,
        Integer sortOrder
) {
}

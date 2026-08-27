package com.innospots.nexus.console.dictionary.domain.request;

/**
 * Request for creating a dictionary item under a type code.
 *
 * @param itemValue stable item value unique within the type
 * @param itemName  display name
 * @param sortOrder display order
 */
public record DictionaryItemCreateRequest(
        String itemValue,
        String itemName,
        Integer sortOrder
) {
}

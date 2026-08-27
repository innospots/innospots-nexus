package com.innospots.nexus.console.dictionary.domain.request;

/**
 * Request for updating mutable dictionary item fields. The item value is immutable.
 *
 * @param itemName  display name
 * @param sortOrder display order
 */
public record DictionaryItemUpdateRequest(
        String itemName,
        Integer sortOrder
) {
}

package com.innospots.nexus.kernel.dictionary;

/**
 * Dictionary entry entity for controlled values.
 *
 * @param entryId   dictionary entry identifier
 * @param typeCode  dictionary type code
 * @param itemCode  item code
 * @param itemName  item display name
 * @param state     lifecycle state
 * @param orderIndex ordering value
 */
public record DictionaryEntry(
        String entryId,
        String typeCode,
        String itemCode,
        String itemName,
        DictionaryState state,
        int orderIndex
) {
}

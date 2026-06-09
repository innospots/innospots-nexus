package com.innospots.nexus.kernel.dictionary;

import java.util.List;
import java.util.Optional;

/**
 * Port for dictionary lookup and maintenance.
 */
public interface DictionaryOperator {

    /**
     * Finds a dictionary entry.
     *
     * @param entryId dictionary entry identifier
     * @return dictionary entry when found
     */
    Optional<DictionaryEntry> findEntry(String entryId);

    /**
     * Lists dictionary entries by type.
     *
     * @param typeCode dictionary type code
     * @return dictionary entries
     */
    List<DictionaryEntry> listEntries(String typeCode);

    /**
     * Saves a dictionary entry.
     *
     * @param entry dictionary entry to save
     * @return saved dictionary entry
     */
    DictionaryEntry saveEntry(DictionaryEntry entry);
}

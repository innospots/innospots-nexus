package com.innospots.nexus.kernel.i18n;

import java.util.List;
import java.util.Optional;

/**
 * Port for internationalization dictionary operations.
 */
public interface I18nDictionaryOperator {

    /**
     * Finds one localized message.
     *
     * @param messageKey message key
     * @param locale     locale tag
     * @return dictionary entry when found
     */
    Optional<I18nDictionaryEntry> findMessage(String messageKey, String locale);

    /**
     * Lists all localized entries for a message key.
     *
     * @param messageKey message key
     * @return localized entries
     */
    List<I18nDictionaryEntry> listMessages(String messageKey);

    /**
     * Saves a localized dictionary entry.
     *
     * @param entry dictionary entry
     * @return saved dictionary entry
     */
    I18nDictionaryEntry saveMessage(I18nDictionaryEntry entry);
}

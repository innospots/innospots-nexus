package com.innospots.nexus.kernel.i18n;

/**
 * Internationalization dictionary entry entity.
 *
 * @param entryId      entry identifier
 * @param scope        entry scope
 * @param messageKey   message key
 * @param locale       locale tag such as zh-CN or en-US
 * @param messageValue translated message value
 */
public record I18nDictionaryEntry(
        String entryId,
        I18nEntryScope scope,
        String messageKey,
        String locale,
        String messageValue
) {
}

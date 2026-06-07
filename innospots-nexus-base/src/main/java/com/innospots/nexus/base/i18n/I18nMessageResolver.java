package com.innospots.nexus.base.i18n;

import java.util.Locale;

/**
 * Strategy for resolving an i18n key to a localized message string.
 * Implementations may load from resource bundles, databases, or
 * any other storage.
 */
@FunctionalInterface
public interface I18nMessageResolver {

    String resolve(String key, Locale locale);
}

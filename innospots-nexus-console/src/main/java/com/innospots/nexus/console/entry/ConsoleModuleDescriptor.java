package com.innospots.nexus.console.entry;

import com.innospots.nexus.base.i18n.I18nObject;

/**
 * Immutable metadata for one built-in console module entry plugin.
 *
 * @param pluginId    reverse-domain plugin identity
 * @param moduleKey   console module key and UiSpec directory name
 * @param pageKey     UiSpec page key, normally {@code {moduleKey}-main}
 * @param pagePath    frontend route path
 * @param menuKey     menu entry key within the module
 * @param menuIcon    optional menu icon
 * @param orderIndex  sibling menu ordering
 * @param displayName module display name
 * @param description module description
 * @param pageTitle   menu and page title
 */
public record ConsoleModuleDescriptor(
        String pluginId,
        String moduleKey,
        String pageKey,
        String pagePath,
        String menuKey,
        String menuIcon,
        int orderIndex,
        I18nObject displayName,
        I18nObject description,
        I18nObject pageTitle
) {

    /**
     * Returns the main page key for a console module.
     *
     * @param moduleKey console module key
     * @return stable page key such as {@code menu-main}
     */
    public static String mainPageKey(String moduleKey) {
        return moduleKey + "-main";
    }
}

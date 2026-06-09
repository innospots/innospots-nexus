package com.innospots.nexus.console.extension;

/**
 * Menu metadata declared by a console extension.
 *
 * @param menuKey    unique menu key within the extension
 * @param title      display title
 * @param path       console navigation path
 * @param orderIndex stable ordering value
 */
public record ConsoleMenuDeclaration(String menuKey, String title, String path, int orderIndex) {
}

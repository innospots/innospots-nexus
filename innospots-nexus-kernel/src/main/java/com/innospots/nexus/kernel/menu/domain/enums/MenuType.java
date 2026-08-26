package com.innospots.nexus.kernel.menu.domain.enums;

/**
 * Structural and navigational menu node types.
 * <p>
 * Authorization actions and API resources intentionally belong to the
 * permission domain rather than this enumeration.
 * </p>
 */
public enum MenuType {

    /**
     * Groups child menu nodes without a navigable destination.
     */
    DIRECTORY,

    /**
     * Renders an internal application page.
     */
    PAGE,

    /**
     * Opens an external URL.
     */
    EXTERNAL_LINK
}

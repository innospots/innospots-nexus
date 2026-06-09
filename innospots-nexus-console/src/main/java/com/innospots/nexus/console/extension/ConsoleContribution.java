package com.innospots.nexus.console.extension;

import java.util.List;
import java.util.Objects;

/**
 * Immutable contribution descriptor published by a management-console
 * extension.
 *
 * @param extensionKey unique extension key
 * @param displayName  human-readable extension name
 * @param menus        menu declarations contributed by the extension
 * @param routes       route declarations contributed by the extension
 */
public record ConsoleContribution(
        String extensionKey,
        String displayName,
        List<ConsoleMenuDeclaration> menus,
        List<ConsoleRouteDeclaration> routes
) {

    /**
     * Creates an immutable console contribution descriptor.
     *
     * @param extensionKey unique extension key
     * @param displayName  human-readable extension name
     * @param menus        menu declarations
     * @param routes       route declarations
     * @return immutable contribution descriptor
     */
    public static ConsoleContribution of(
            String extensionKey,
            String displayName,
            List<ConsoleMenuDeclaration> menus,
            List<ConsoleRouteDeclaration> routes
    ) {
        return new ConsoleContribution(
                Objects.requireNonNull(extensionKey, "extensionKey must not be null"),
                Objects.requireNonNull(displayName, "displayName must not be null"),
                menus == null ? List.of() : List.copyOf(menus),
                routes == null ? List.of() : List.copyOf(routes)
        );
    }
}

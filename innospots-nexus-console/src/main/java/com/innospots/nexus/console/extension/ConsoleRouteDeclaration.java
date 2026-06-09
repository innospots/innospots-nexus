package com.innospots.nexus.console.extension;

/**
 * Route metadata declared by a console extension.
 *
 * @param routeKey  unique route key within the extension
 * @param path      console route path
 * @param component logical component identifier supplied by an adapter module
 */
public record ConsoleRouteDeclaration(String routeKey, String path, String component) {
}

package com.innospots.nexus.kernel.permission;

/**
 * Permission entity representing an action over a resource.
 *
 * @param permissionId permission identifier
 * @param resource     protected resource code
 * @param action       action code
 * @param effect       permission effect
 * @param description  permission description
 */
public record Permission(
        String permissionId,
        String resource,
        String action,
        PermissionEffect effect,
        String description
) {
}

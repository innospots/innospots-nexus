package com.innospots.nexus.kernel.role;

/**
 * Role entity used to group permissions.
 *
 * @param roleId      role identifier
 * @param code        stable role code
 * @param name        role name
 * @param scope       role scope
 * @param description role description
 */
public record Role(String roleId, String code, String name, RoleScope scope, String description) {
}

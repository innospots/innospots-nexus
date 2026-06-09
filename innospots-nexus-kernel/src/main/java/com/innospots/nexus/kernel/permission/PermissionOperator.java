package com.innospots.nexus.kernel.permission;

import java.util.List;

/**
 * Port for permission assignment and evaluation.
 */
public interface PermissionOperator {

    /**
     * Lists permissions granted to a role.
     *
     * @param roleId role identifier
     * @return role permissions
     */
    List<Permission> listRolePermissions(String roleId);

    /**
     * Saves a permission definition.
     *
     * @param permission permission to save
     * @return saved permission
     */
    Permission savePermission(Permission permission);

    /**
     * Checks whether a user can perform an action on a resource.
     *
     * @param userId   user identifier
     * @param resource resource code
     * @param action   action code
     * @return permission effect
     */
    PermissionEffect check(String userId, String resource, String action);
}

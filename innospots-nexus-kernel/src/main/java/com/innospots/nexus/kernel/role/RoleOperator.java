package com.innospots.nexus.kernel.role;

import java.util.List;
import java.util.Optional;

/**
 * Port for role management operations.
 */
public interface RoleOperator {

    /**
     * Finds a role by identifier.
     *
     * @param roleId role identifier
     * @return role when found
     */
    Optional<Role> findRole(String roleId);

    /**
     * Lists roles assigned to a user.
     *
     * @param userId user identifier
     * @return assigned roles
     */
    List<Role> listUserRoles(String userId);

    /**
     * Saves a role.
     *
     * @param role role to save
     * @return saved role
     */
    Role saveRole(Role role);
}

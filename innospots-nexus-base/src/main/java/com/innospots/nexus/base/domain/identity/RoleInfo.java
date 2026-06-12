package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

import java.util.List;

/**
 * A role definition with a unique identifier, display name, and
 * programmatic code.  Used as a domain value object across module
 * boundaries.
 *
 * @see com.innospots.nexus.kernel.role.domain.entity.RoleEntity
 */
public record RoleInfo(
        String roleId,
        String roleName,
        String roleCode,
        BasicStatus status,
        List<String> userIds
) {

    /**
     * Returns a role info without any member user IDs.
     */
    public static RoleInfo of(String roleId, String roleName, String roleCode, BasicStatus status) {
        return new RoleInfo(roleId, roleName, roleCode, status, List.of());
    }
}

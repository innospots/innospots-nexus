package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * A role definition with a unique identifier, display name, and
 * programmatic code.
 */
public record RoleInfo(
        Long roleId,
        String roleName,
        String roleCode,
        BasicStatus status
) {
}

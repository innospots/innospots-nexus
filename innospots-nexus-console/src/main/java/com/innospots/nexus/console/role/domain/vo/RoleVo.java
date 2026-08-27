package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * Management-console role view.
 *
 * @param roleId         role identifier
 * @param roleName       display name
 * @param roleCode       stable code unique within the owner
 * @param ownerType      ownership layer
 * @param ownerId        owner identifier; empty for PLATFORM
 * @param securityRealm  PLATFORM or TENANT
 * @param description    optional description
 * @param status         lifecycle status
 * @param sortOrder      display order
 * @param builtIn        whether the role is system-managed
 * @param administrator  whether the role bypasses ordinary resource checks
 * @param memberCount    assigned user count
 * @param createdAt      creation time
 * @param updatedAt      last update time
 */
public record RoleVo(
        String roleId,
        String roleName,
        String roleCode,
        RoleOwnerType ownerType,
        String ownerId,
        SecurityRealm securityRealm,
        String description,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        Boolean administrator,
        long memberCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

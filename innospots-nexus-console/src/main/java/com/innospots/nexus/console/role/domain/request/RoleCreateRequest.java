package com.innospots.nexus.console.role.domain.request;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * Request for creating a role owned by a platform, tenant, or workspace node.
 *
 * @param roleName       display name
 * @param roleCode       stable code unique within the owner
 * @param ownerType      ownership layer
 * @param ownerId        owner identifier; empty for PLATFORM
 * @param securityRealm  PLATFORM or TENANT
 * @param description    optional role description
 * @param sortOrder      display order
 */
public record RoleCreateRequest(
        String roleName,
        String roleCode,
        RoleOwnerType ownerType,
        String ownerId,
        SecurityRealm securityRealm,
        String description,
        Integer sortOrder
) {
}

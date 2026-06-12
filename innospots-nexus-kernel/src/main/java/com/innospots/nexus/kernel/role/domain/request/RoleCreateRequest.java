package com.innospots.nexus.kernel.role.domain.request;

/**
 * Request for creating a project role.
 *
 * @param roleName    display name
 * @param roleCode    stable project-unique code
 * @param description optional role description
 * @param sortOrder   display order
 */
public record RoleCreateRequest(
        String roleName,
        String roleCode,
        String description,
        Integer sortOrder
) {
}

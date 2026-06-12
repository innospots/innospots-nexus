package com.innospots.nexus.kernel.role.domain.request;

/**
 * Request for updating mutable role profile fields.
 *
 * @param roleName    display name
 * @param description optional role description
 * @param sortOrder   display order
 */
public record RoleUpdateRequest(
        String roleName,
        String description,
        Integer sortOrder
) {
}

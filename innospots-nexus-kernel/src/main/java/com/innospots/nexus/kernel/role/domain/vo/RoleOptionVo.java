package com.innospots.nexus.kernel.role.domain.vo;

/**
 * Compact role option for selectors and assignment forms.
 *
 * @param roleId        role identifier
 * @param roleName      display name
 * @param roleCode      stable code
 * @param administrator whether this is an administrator role
 */
public record RoleOptionVo(
        String roleId,
        String roleName,
        String roleCode,
        Boolean administrator
) {
}

package com.innospots.nexus.kernel.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Management-console role view.
 *
 * @param roleId        role identifier
 * @param roleName      display name
 * @param roleCode      stable project-unique code
 * @param description   optional description
 * @param status        lifecycle status
 * @param sortOrder     display order
 * @param builtIn       whether the role is system-managed
 * @param administrator whether the role bypasses ordinary resource checks
 * @param memberCount   assigned user count
 * @param createdTime   creation time
 * @param updatedTime   last update time
 */
public record RoleVo(
        String roleId,
        String roleName,
        String roleCode,
        String description,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        Boolean administrator,
        long memberCount,
        LocalDateTime createdTime,
        LocalDateTime updatedTime
) {
}

package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * Role binding displayed in assignment management.
 *
 * @param bindingId   binding identifier
 * @param roleId      bound role identifier
 * @param subjectType USER or ORG_UNIT
 * @param subjectId   subject identifier
 * @param createdAt   assignment time
 */
public record RoleBindingVo(
        String bindingId,
        String roleId,
        RoleBindingSubjectType subjectType,
        String subjectId,
        LocalDateTime createdAt
) {
}

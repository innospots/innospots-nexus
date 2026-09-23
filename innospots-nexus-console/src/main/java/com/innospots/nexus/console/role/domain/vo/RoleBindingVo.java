package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * 分配管理中展示的角色绑定。
 *
 * @author Smars
 * @date 2026/09/13
 * @param bindingId   binding 标识符
 * @param roleId      bound 角色标识符
 * @param subjectType USER 或 ORG_UNIT
 * @param subjectId   subject 标识符
 * @param createdAt   分配时间
 */
public record RoleBindingVo(
        String bindingId,
        String roleId,
        RoleBindingSubjectType subjectType,
        String subjectId,
        LocalDateTime createdAt
) {
}

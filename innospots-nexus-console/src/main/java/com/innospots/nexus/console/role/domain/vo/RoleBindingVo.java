package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

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
@Schema(name = "RoleBindingVo", description = "角色绑定视图")
public record RoleBindingVo(
        @Schema(description = "binding 标识符", required = true)
        String bindingId,
        @Schema(description = "bound 角色标识符", required = true)
        String roleId,
        @Schema(description = "USER 或 ORG_UNIT", required = true)
        RoleBindingSubjectType subjectType,
        @Schema(description = "subject 标识符", required = true)
        String subjectId,
        @Schema(description = "分配时间", required = true)
        LocalDateTime createdAt
) {
}

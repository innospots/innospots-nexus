package com.innospots.nexus.console.role.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 用于选择器与分配表单的角色精简选项。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleId        角色标识符
 * @param roleName      显示名称
 * @param roleCode      稳定编码
 * @param administrator 是否为管理员角色
 */
@Schema(name = "RoleOptionVo", description = "角色选项")
public record RoleOptionVo(
        @Schema(description = "角色标识符", required = true)
        String roleId,
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "稳定编码", required = true)
        String roleCode,
        @Schema(description = "是否为管理员角色", required = true)
        Boolean administrator
) {
}

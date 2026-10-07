package com.innospots.nexus.console.role.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新可变角色档案字段的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleName    显示名称
 * @param description 可选 role 描述
 * @param sortOrder   显示顺序
 */
@Schema(name = "RoleUpdateRequest", description = "更新角色请求")
public record RoleUpdateRequest(
        @Schema(description = "显示名称", required = true)
        String roleName,
        @Schema(description = "可选角色描述")
        String description,
        @Schema(description = "显示顺序")
        Integer sortOrder
) {
}

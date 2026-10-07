package com.innospots.nexus.console.role.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 启用或禁用角色的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status 目标角色状态
 */
@Schema(name = "RoleStatusUpdateRequest", description = "更新角色状态请求")
public record RoleStatusUpdateRequest(
        @Schema(description = "目标角色状态", required = true)
        BasicStatus status
) {
}

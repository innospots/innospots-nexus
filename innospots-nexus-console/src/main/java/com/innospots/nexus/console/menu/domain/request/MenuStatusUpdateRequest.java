package com.innospots.nexus.console.menu.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 菜单生命周期状态更新。
 *
 * @author Smars
 * @date 2026/09/13
 * @param status target 生命周期状态
 */
@Schema(name = "MenuStatusUpdateRequest", description = "更新菜单状态请求")
public record MenuStatusUpdateRequest(
        @Schema(description = "目标生命周期状态", required = true)
        BasicStatus status
) {
}

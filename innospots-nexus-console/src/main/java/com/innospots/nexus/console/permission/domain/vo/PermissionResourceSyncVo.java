package com.innospots.nexus.console.permission.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 显式同步扩展和 UiSpec 权限目录后的处理结果。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Schema(name = "PermissionResourceSyncVo", description = "权限资源同步结果")
public record PermissionResourceSyncVo(
        @Schema(description = "新创建的资源数量", required = true)
        int createdResources,
        @Schema(description = "元数据发生变化并被更新的资源数量", required = true)
        int updatedResources,
        @Schema(description = "当前来源中已不存在、被标记为禁用的资源数量", required = true)
        int disabledResources
) {
}

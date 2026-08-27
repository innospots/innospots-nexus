package com.innospots.nexus.console.permission.domain.vo;

/** 显式同步扩展和 UiSpec 权限目录后的处理结果。 */
public record PermissionResourceSyncVo(
        /** 新创建的资源数量。 */
        int createdResources,
        /** 元数据发生变化并被更新的资源数量。 */
        int updatedResources,
        /** 当前来源中已不存在、被标记为禁用的资源数量。 */
        int disabledResources
) {
}

package com.innospots.nexus.console.role.domain.request;

/**
 * 更新可变角色档案字段的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleName    显示名称
 * @param description 可选 role 描述
 * @param sortOrder   显示顺序
 */
public record RoleUpdateRequest(
        String roleName,
        String description,
        Integer sortOrder
) {
}

package com.innospots.nexus.console.role.domain.vo;

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
public record RoleOptionVo(
        String roleId,
        String roleName,
        String roleCode,
        Boolean administrator
) {
}

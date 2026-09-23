package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

import java.util.List;

/**
 * 角色定义，包含唯一标识符、显示名称与程序化编码。
 * 作为跨模块边界的领域值对象使用。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleId   角色 ID
 * @param roleName 角色名称
 * @param roleCode 角色编码
 * @param status   状态
 * @param userIds  成员用户 ID 列表
 * @see com.innospots.nexus.console.role.domain.entity.RoleEntity
 */
public record RoleSnapshot(
        String roleId,
        String roleName,
        String roleCode,
        BasicStatus status,
        List<String> userIds
) {

    /**
     * 创建不含成员用户 ID 的角色信息。
     *
     * @param roleId   角色 ID
     * @param roleName 角色名称
     * @param roleCode 角色编码
     * @param status   状态
     * @return 角色快照
     */
    public static RoleSnapshot of(String roleId, String roleName, String roleCode, BasicStatus status) {
        return new RoleSnapshot(roleId, roleName, roleCode, status, List.of());
    }
}

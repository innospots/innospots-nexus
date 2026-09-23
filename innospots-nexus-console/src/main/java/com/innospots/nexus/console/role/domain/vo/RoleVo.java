package com.innospots.nexus.console.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 管理控制台角色视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleId         角色标识符
 * @param roleName       显示名称
 * @param roleCode       归属范围内唯一的稳定编码
 * @param ownerType      归属层级
 * @param ownerId        owner 标识符; empty for PLATFORM
 * @param securityRealm  PLATFORM 或 TENANT
 * @param description    可选 描述
 * @param status         生命周期状态
 * @param sortOrder      显示顺序
 * @param builtIn        角色是否由系统管理
 * @param administrator  角色是否绕过普通资源检查
 * @param memberCount    已分配用户数量
 * @param createdAt      创建时间
 * @param updatedAt      最后更新时间
 */
public record RoleVo(
        String roleId,
        String roleName,
        String roleCode,
        RoleOwnerType ownerType,
        String ownerId,
        SecurityRealm securityRealm,
        String description,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        Boolean administrator,
        long memberCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

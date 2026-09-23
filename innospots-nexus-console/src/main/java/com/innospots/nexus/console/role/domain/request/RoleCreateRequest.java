package com.innospots.nexus.console.role.domain.request;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 创建由平台、租户或工作区节点拥有的角色的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param roleName       显示名称
 * @param roleCode       归属范围内唯一的稳定编码
 * @param ownerType      归属层级
 * @param ownerId        owner 标识符; empty for PLATFORM
 * @param securityRealm  PLATFORM 或 TENANT
 * @param description    可选 role 描述
 * @param sortOrder      显示顺序
 */
public record RoleCreateRequest(
        String roleName,
        String roleCode,
        RoleOwnerType ownerType,
        String ownerId,
        SecurityRealm securityRealm,
        String description,
        Integer sortOrder
) {
}

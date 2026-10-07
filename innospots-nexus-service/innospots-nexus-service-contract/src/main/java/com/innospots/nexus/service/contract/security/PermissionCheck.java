package com.innospots.nexus.service.contract.security;

import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * 针对资源的权限检查。
 *
 * @param operationId 操作标识
 * @param permissions 所需权限键
 * @param resource    目标资源
 * @author Smars
 * @date 2026/09/13
 * @see PermissionProvider
 */
public record PermissionCheck(String operationId, Set<String> permissions, ResourceRef resource) {

    public PermissionCheck {
        Checks.notBlank(operationId, "operationId");
        Checks.notNull(resource, "resource");
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
    }
}

package com.innospots.nexus.service.contract.security;

import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * Permission check against a resource.
 *
 * @param operationId operation identifier
 * @param permissions required permission keys
 * @param resource    target resource
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

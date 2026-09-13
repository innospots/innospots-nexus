package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * Resource identity used by authorization and audit.
 *
 * @param type  resource type
 * @param id    resource identifier
 * @param scope owning scope
 * @author Smars
 * @date 2026/09/13
 * @see ServiceScope
 */
public record ResourceRef(String type, String id, ServiceScope scope) {

    public ResourceRef {
        Checks.notBlank(type, "type");
        Checks.notBlank(id, "id");
        Checks.notNull(scope, "scope");
    }
}

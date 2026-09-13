package com.innospots.nexus.service.contract.security;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * Immutable caller identity. Collections are copied. Anonymous principals use id {@code anonymous}.
 *
 * @param id          principal identifier
 * @param type        principal kind
 * @param realm       authentication realm
 * @param roles       declared roles, not a permission bypass
 * @param permissions declared permission keys
 * @param attributes  additional identity attributes
 * @author Smars
 * @date 2026/09/13
 * @see PrincipalType
 * @see ServiceScope
 */
public record ServicePrincipal(
        String id,
        PrincipalType type,
        String realm,
        Set<String> roles,
        Set<String> permissions,
        Map<String, String> attributes
) {

    public static final String ANONYMOUS_ID = "anonymous";

    public ServicePrincipal {
        Checks.notNull(type, "type");
        Checks.notBlank(realm, "realm");
        if (type == PrincipalType.ANONYMOUS) {
            Checks.isTrue(ANONYMOUS_ID.equals(id), "anonymous principal id must be anonymous");
        } else {
            Checks.notBlank(id, "id");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    /**
     * Returns an anonymous principal for {@code realm}.
     *
     * @param realm authentication realm
     * @return anonymous principal
     */
    public static ServicePrincipal anonymous(String realm) {
        return new ServicePrincipal(ANONYMOUS_ID, PrincipalType.ANONYMOUS, realm, Set.of(), Set.of(), Map.of());
    }
}

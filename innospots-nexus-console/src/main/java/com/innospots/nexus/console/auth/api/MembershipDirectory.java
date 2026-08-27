package com.innospots.nexus.console.auth.api;

import java.util.List;

import com.innospots.nexus.console.auth.domain.model.TenantMembership;

/**
 * Tenant membership lookup used after tenant-realm identity authentication.
 */
public interface MembershipDirectory {

    /**
     * Lists active memberships for a tenant-realm user.
     *
     * @param tenantUserId tenant-realm user id
     * @return active memberships, never null
     */
    List<TenantMembership> listActiveMemberships(String tenantUserId);

    /**
     * Lists tenant ids for which the tenant user has an ACTIVE membership.
     *
     * @param tenantUserId tenant-realm user id
     * @return active tenant identifiers, never null
     */
    default List<String> listActiveTenantIds(String tenantUserId) {
        return listActiveMemberships(tenantUserId).stream()
                .map(TenantMembership::tenantId)
                .toList();
    }
}

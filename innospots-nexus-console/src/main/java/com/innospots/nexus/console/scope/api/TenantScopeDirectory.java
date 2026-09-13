package com.innospots.nexus.console.scope.api;

import java.util.Optional;

import com.innospots.nexus.console.scope.domain.model.TenantScope;

/**
 * Loads tenant and business organization snapshots for session binding.
 */
public interface TenantScopeDirectory {

    /**
     * Finds tenant scope snapshots by tenant identifier.
     *
     * @param tenantId tenant identifier
     * @return tenant scope when found
     */
    Optional<TenantScope> findByTenantId(String tenantId);
}

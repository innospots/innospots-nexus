package com.innospots.nexus.service.contract.security;

import java.util.concurrent.CompletionStage;

/**
 * Authorizes a principal against a {@link PermissionCheck}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see PermissionCheck
 * @see PermissionDecision
 */
public interface PermissionProvider {

    /**
     * Authorizes {@code principal} in {@code scope} for {@code check}.
     *
     * @param principal caller
     * @param scope     resource scope
     * @param check     permission request
     * @return decision
     */
    CompletionStage<PermissionDecision> authorize(
            ServicePrincipal principal,
            ServiceScope scope,
            PermissionCheck check);
}

package com.innospots.nexus.service.contract.security;

import java.util.concurrent.CompletionStage;

import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * Host IAM adapter that authenticates a pre-auth {@link ServiceContext}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see AuthenticationResult
 * @see PermissionProvider
 */
public interface SecurityProvider {

    /**
     * Returns the provider identifier.
     *
     * @return stable id
     */
    String id();

    /**
     * Authenticates the current request. Credentials stay in the native request scope.
     *
     * @param context pre-auth context
     * @return authentication result
     */
    CompletionStage<AuthenticationResult> authenticate(ServiceContext context);
}

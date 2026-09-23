package com.innospots.nexus.service.runtime.security;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InterceptorIds;
import com.innospots.nexus.service.contract.invocation.InterceptorOrders;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.security.AuthenticationResult;
import com.innospots.nexus.service.contract.security.PrincipalType;
import com.innospots.nexus.service.contract.security.SecurityProvider;
import com.innospots.nexus.service.contract.security.ServicePrincipal;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;

/**
 * 调用链认证协调器。成功后将已认证上下文安装到线程绑定访问器。
 *
 * @author Smars
 * @date 2026/09/15
 * @see AuthorizationInterceptor
 * @see SecurityProvider
 */
public final class AuthenticationCoordinator implements ServiceInterceptor {

    private final SecurityProvider securityProvider;
    private final ThreadBoundServiceContext contexts;

    /**
     * 创建协调器。
     *
     * @param securityProvider 宿主 IAM 提供者
     * @param contexts         线程绑定上下文
     */
    public AuthenticationCoordinator(SecurityProvider securityProvider, ThreadBoundServiceContext contexts) {
        this.securityProvider = Checks.notNull(securityProvider, "securityProvider");
        this.contexts = Checks.notNull(contexts, "contexts");
    }

    @Override
    public String id() {
        return InterceptorIds.AUTHN;
    }

    @Override
    public int order() {
        return InterceptorOrders.AUTHN;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        if (invocation.policy().permissionKeys().isEmpty()) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        ServiceContext current = contexts.requireCurrent();
        ServicePrincipal principal = current.security();
        if (principal.type() != PrincipalType.ANONYMOUS) {
            validateScope(current.scope());
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        return securityProvider.authenticate(current)
                .thenApply(result -> {
                    AuthenticationResult authenticated = Checks.notNull(result, "authentication result");
                    rejectAnonymous(authenticated.principal());
                    validateScope(authenticated.scope());
                    rejectExpired(authenticated);
                    contexts.install(replaceSecurity(current, authenticated));
                    return (InvocationLease) outcome -> CompletableFuture.completedFuture(null);
                });
    }

    private static void rejectAnonymous(ServicePrincipal principal) {
        if (principal.type() == PrincipalType.ANONYMOUS) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
    }

    private static void rejectExpired(AuthenticationResult result) {
        Instant expiresAt = result.expiresAt();
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
    }

    private static void validateScope(ServiceScope scope) {
        Checks.notNull(scope, "scope");
    }

    private static ServiceContext replaceSecurity(ServiceContext current, AuthenticationResult result) {
        return new ServiceContext(
                current.requestId(),
                current.request(),
                result.principal(),
                result.scope(),
                current.trace(),
                current.cancellation(),
                current.deadline(),
                current.attributes());
    }
}

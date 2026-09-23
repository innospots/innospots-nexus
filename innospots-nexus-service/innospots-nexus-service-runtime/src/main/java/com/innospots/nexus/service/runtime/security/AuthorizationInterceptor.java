package com.innospots.nexus.service.runtime.security;

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
import com.innospots.nexus.service.contract.security.PermissionCheck;
import com.innospots.nexus.service.contract.security.PermissionDecision;
import com.innospots.nexus.service.contract.security.PermissionProvider;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;

/**
 * 调用链授权拦截器。
 *
 * @author Smars
 * @date 2026/09/15
 * @see AuthenticationCoordinator
 * @see PermissionProvider
 */
public final class AuthorizationInterceptor implements ServiceInterceptor {

    private final PermissionProvider permissionProvider;
    private final ThreadBoundServiceContext contexts;

    /**
     * 创建拦截器。
     *
     * @param permissionProvider 权限提供者
     * @param contexts           线程绑定上下文
     */
    public AuthorizationInterceptor(PermissionProvider permissionProvider, ThreadBoundServiceContext contexts) {
        this.permissionProvider = Checks.notNull(permissionProvider, "permissionProvider");
        this.contexts = Checks.notNull(contexts, "contexts");
    }

    @Override
    public String id() {
        return InterceptorIds.AUTHZ;
    }

    @Override
    public int order() {
        return InterceptorOrders.AUTHZ;
    }

    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        if (invocation.policy().permissionKeys().isEmpty()) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        ServiceContext current = contexts.requireCurrent();
        PermissionCheck check = new PermissionCheck(
                invocation.operationId(),
                invocation.policy().permissionKeys(),
                invocation.resource());
        return permissionProvider.authorize(current.security(), current.scope(), check)
                .thenApply(decision -> {
                    PermissionDecision resolved = Checks.notNull(decision, "permission decision");
                    if (!resolved.allowed()) {
                        throw NexusException.build(NexusStatusCode.NO_PERMISSION);
                    }
                    return (InvocationLease) outcome -> CompletableFuture.completedFuture(null);
                });
    }
}

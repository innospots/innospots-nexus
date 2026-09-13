package com.innospots.nexus.service.contract.invocation;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.security.ResourceRef;

/**
 * Runtime invocation envelope passed to interceptors.
 *
 * @param invocationId unique invocation identifier
 * @param operationId  stable operation identifier
 * @param service      current service context
 * @param policy       resolved operation policy
 * @param resource     target resource
 * @author Smars
 * @date 2026/09/13
 * @see ServiceInterceptor
 * @see OperationPolicy
 */
public record InvocationContext(
        String invocationId,
        String operationId,
        ServiceContext service,
        OperationPolicy policy,
        ResourceRef resource
) {

    public InvocationContext {
        Checks.notBlank(invocationId, "invocationId");
        Checks.notBlank(operationId, "operationId");
        Checks.notNull(service, "service");
        Checks.notNull(policy, "policy");
        Checks.notNull(resource, "resource");
    }
}

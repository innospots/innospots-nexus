package com.innospots.nexus.service.contract.invocation;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.security.ResourceRef;

/**
 * 传递给拦截器的运行时调用封装。
 *
 * @param invocationId 唯一调用标识
 * @param operationId  稳定操作标识
 * @param service      当前服务上下文
 * @param policy       已解析操作策略
 * @param resource     目标资源
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

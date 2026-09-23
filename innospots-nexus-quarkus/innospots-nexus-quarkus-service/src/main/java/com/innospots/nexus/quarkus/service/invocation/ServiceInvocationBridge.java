package com.innospots.nexus.quarkus.service.invocation;

import java.lang.reflect.Method;
import java.util.UUID;

import com.innospots.nexus.quarkus.service.config.ServiceRuntimeHolder;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.policy.OperationPolicy;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * 通过 {@link InvocationEngine} 执行带策略的业务调用。
 */
@ApplicationScoped
public final class ServiceInvocationBridge {

    private final ServiceRuntimeHolder serviceRuntimeHolder;
    private final AnnotationPolicyResolver policyResolver;

    public ServiceInvocationBridge(
            ServiceRuntimeHolder serviceRuntimeHolder,
            AnnotationPolicyResolver policyResolver) {
        this.serviceRuntimeHolder = Checks.notNull(serviceRuntimeHolder, "serviceRuntimeHolder");
        this.policyResolver = Checks.notNull(policyResolver, "policyResolver");
    }

    /**
     * 执行带注解策略的方法调用。
     *
     * @param target        目标对象
     * @param method        目标方法
     * @param operationId   操作标识
     * @param business      业务逻辑
     * @param <T>           结果类型
     * @return 业务结果
     */
    public <T> T invoke(Object target, Method method, String operationId, java.util.function.Supplier<T> business) {
        Checks.notNull(target, "target");
        Checks.notNull(method, "method");
        Checks.notBlank(operationId, "operationId");
        Checks.notNull(business, "business");
        ServiceContext service = serviceRuntimeHolder.contexts().requireCurrent();
        OperationPolicy policy = policyResolver.resolve(target.getClass(), method);
        InvocationContext context = new InvocationContext(
                UUID.randomUUID().toString(),
                operationId,
                service,
                policy,
                new ResourceRef("adapter", operationId, ServiceScope.platform()));
        return serviceRuntimeHolder.invocationEngine().invokeSync(context, business);
    }
}

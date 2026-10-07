package com.innospots.nexus.spring.service.http.invocation;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.security.ResourceRef;
import com.innospots.nexus.service.contract.security.ServiceScope;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.service.runtime.invocation.InvocationEngine;
import com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver;

/**
 * 构建 {@link InvocationContext} 并经 {@link InvocationEngine} 执行（供 Bridge 与 AOP 共用）。
 */
public final class GovernedInvocationExecutor {

    private final InvocationEngine engine;
    private final ThreadBoundServiceContext contexts;
    private final AnnotationPolicyResolver policyResolver;

    /**
     * 创建执行器。
     *
     * @param engine         调用引擎
     * @param contexts       线程绑定上下文
     * @param policyResolver 注解策略解析器
     */
    public GovernedInvocationExecutor(
            InvocationEngine engine,
            ThreadBoundServiceContext contexts,
            AnnotationPolicyResolver policyResolver) {
        this.engine = Checks.notNull(engine, "engine");
        this.contexts = Checks.notNull(contexts, "contexts");
        this.policyResolver = Checks.notNull(policyResolver, "policyResolver");
    }

    /**
     * 同步执行。
     *
     * @param declaringType 策略注解声明类型
     * @param method        目标方法
     * @param operationId   操作标识
     * @param business      业务逻辑
     * @param <T>           结果类型
     * @return 业务结果
     */
    public <T> T invokeSync(Class<?> declaringType, Method method, String operationId, Supplier<T> business) {
        Checks.notNull(declaringType, "declaringType");
        Checks.notNull(method, "method");
        Checks.notBlank(operationId, "operationId");
        Checks.notNull(business, "business");
        InvocationContext context = buildContext(declaringType, method, operationId);
        GovernedInvocationGuard.enter();
        try {
            return engine.invokeSync(context, business);
        } finally {
            GovernedInvocationGuard.leave();
        }
    }

    /**
     * 流式执行。
     *
     * @param declaringType 策略注解声明类型
     * @param method        目标方法
     * @param operationId   操作标识
     * @param business      发布者供应方
     * @param <T>           元素类型
     * @return 发布者
     */
    public <T> Flow.Publisher<T> invokeStream(
            Class<?> declaringType,
            Method method,
            String operationId,
            Supplier<Flow.Publisher<T>> business) {
        Checks.notNull(declaringType, "declaringType");
        Checks.notNull(method, "method");
        Checks.notBlank(operationId, "operationId");
        Checks.notNull(business, "business");
        InvocationContext context = buildContext(declaringType, method, operationId);
        return engine.invokeStream(context, business);
    }

    /**
     * 构建调用上下文。
     *
     * @param declaringType 声明类型
     * @param method        方法
     * @param operationId   操作标识
     * @return 上下文
     */
    public InvocationContext buildContext(Class<?> declaringType, Method method, String operationId) {
        ServiceContext service = contexts.requireCurrent();
        var policy = policyResolver.resolve(declaringType, method);
        return new InvocationContext(
                UUID.randomUUID().toString(),
                operationId,
                service,
                policy,
                new ResourceRef("adapter", operationId, ServiceScope.platform()));
    }
}

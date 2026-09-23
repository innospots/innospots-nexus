package com.innospots.nexus.spring.service.http.invocation;

import java.lang.reflect.Method;
import java.util.concurrent.Flow;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.innospots.nexus.base.util.Checks;

/**
 * 通过 {@link GovernedInvocationExecutor} 执行带策略的业务调用。
 *
 * <p>与 {@link com.innospots.nexus.spring.service.http.governance.GovernedInvocationAspect} 共用引擎；
 * 显式 {@code operationId} 时优先于 {@link ServiceOperationIdResolver}。</p>
 */
@Component
public final class ServiceInvocationBridge {

    private final GovernedInvocationExecutor executor;

    /**
     * 创建调用桥。
     *
     * @param executor 治理调用执行器
     */
    public ServiceInvocationBridge(GovernedInvocationExecutor executor) {
        this.executor = Checks.notNull(executor, "executor");
    }

    /**
     * 执行带注解策略的方法调用。
     *
     * @param target      目标对象
     * @param method      目标方法
     * @param operationId 操作标识
     * @param business    业务逻辑
     * @param <T>         结果类型
     * @return 业务结果
     */
    public <T> T invoke(Object target, Method method, String operationId, Supplier<T> business) {
        Checks.notNull(target, "target");
        Checks.notNull(method, "method");
        return executor.invokeSync(method.getDeclaringClass(), method, operationId, business);
    }

    /**
     * 执行带注解策略的流式调用；返回的 {@link Flow.Publisher} 仅允许订阅一次。
     *
     * @param target      目标对象
     * @param method      目标方法
     * @param operationId 操作标识
     * @param business    返回业务 {@link Flow.Publisher} 的供应方
     * @param <T>         流元素类型
     * @return 经拦截器链包装后的 Publisher
     */
    public <T> Flow.Publisher<T> invokeStream(
            Object target,
            Method method,
            String operationId,
            Supplier<Flow.Publisher<T>> business) {
        Checks.notNull(target, "target");
        Checks.notNull(method, "method");
        return executor.invokeStream(method.getDeclaringClass(), method, operationId, business);
    }
}

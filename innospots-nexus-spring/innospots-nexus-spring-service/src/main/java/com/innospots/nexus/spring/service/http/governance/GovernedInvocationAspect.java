package com.innospots.nexus.spring.service.http.governance;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.spring.service.http.invocation.GovernedInvocationExecutor;
import com.innospots.nexus.spring.service.http.invocation.GovernedInvocationGuard;
import com.innospots.nexus.spring.service.http.invocation.ServiceOperationIdResolver;

/**
 * 对 REST Controller 上声明的契约注解自动接入 {@link com.innospots.nexus.service.runtime.invocation.InvocationEngine}。
 */
@Aspect
public final class GovernedInvocationAspect {

    private final GovernedInvocationExecutor executor;

    /**
     * 创建切面。
     *
     * @param executor 治理调用执行器
     */
    public GovernedInvocationAspect(GovernedInvocationExecutor executor) {
        this.executor = Checks.notNull(executor, "executor");
    }

    /**
     * 在带治理/安全/操作标识注解的 Controller 方法外包裹引擎调用链。
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 业务或引擎异常
     */
    @Around("governedControllerMethod()")
    public Object aroundGovernedController(ProceedingJoinPoint joinPoint) throws Throwable {
        if (GovernedInvocationGuard.isActive()) {
            return joinPoint.proceed();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringType = signature.getDeclaringType();
        String operationId = ServiceOperationIdResolver.resolve(declaringType, method);
        return executor.invokeSync(declaringType, method, operationId, () -> proceedUnchecked(joinPoint));
    }

    /**
     * 匹配须进入 InvocationEngine 的 Controller 方法。
     */
    @org.aspectj.lang.annotation.Pointcut(
            "@within(org.springframework.web.bind.annotation.RestController)"
                    + " && ("
                    + "@annotation(com.innospots.nexus.service.contract.policy.annotation.ServiceOperation)"
                    + " || @annotation(com.innospots.nexus.service.contract.policy.annotation.RateLimited)"
                    + " || @annotation(com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected)"
                    + " || @annotation(com.innospots.nexus.service.contract.policy.annotation.CircuitProtected)"
                    + " || @annotation(com.innospots.nexus.service.contract.policy.annotation.TimeoutProtected)"
                    + " || @annotation(com.innospots.nexus.service.contract.security.annotation.RequiresPermission)"
                    + " || @annotation(com.innospots.nexus.service.contract.audit.annotation.Audited)"
                    + " || @annotation(com.innospots.nexus.service.contract.security.annotation.PublicAccess)"
                    + ")")
    void governedControllerMethod() {
    }

    private static Object proceedUnchecked(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (throwable instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(throwable);
        }
    }
}

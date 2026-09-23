package com.innospots.nexus.console.logger;

import java.util.Objects;

import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

/**
 * 所有框架适配器共享的可复用拦截例程。
 * <p>使用 {@link Callback} 包装调用，记录耗时与结果，并委托给
 * {@link InvocationLogHandler}。框架适配器（AspectJ、Byte Buddy、
 * CDI 等）在标注方法周围调用 {@link #execute}；本类从不
 * 依赖任何拦截框架。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class LogExecutor {

    private final InvocationLogHandler handler;

    /**
     * 创建委托给定处理器的执行器。
     *
     * @param handler 目标处理器，不可为 null
     */
    public LogExecutor(InvocationLogHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler must not be null");
    }

    /**
     * 执行回调并为调用记录审计上下文。
     * <p>回调结果原样重抛；审计记录
     * 即使失败也通过配置的处理器产生。</p>
     *
     * @param auditLog   声明捕获内容的注解
     * @param className  被拦截方法的声明类名
     * @param methodName 被拦截的方法名
     * @param actor      操作用户身份；未知时为 null
     * @param arguments  被拦截的方法参数；无则为 null
     * @param callback   实际的方法调用
     * @return 回调结果
     * @throws Throwable 原样重抛回调结果
     */
    public Object execute(
            AuditLog auditLog,
            String className,
            String methodName,
            String actor,
            Object[] arguments,
            Callback callback) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable exception = null;
        try {
            result = callback.call();
            return result;
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            InvocationLogContext context = new InvocationLogContext(
                    className,
                    methodName,
                    auditLog.action(),
                    auditLog.recordArgs() ? arguments : null,
                    auditLog.recordResult() ? result : null,
                    auditLog.recordException() ? exception : null,
                    startTime,
                    System.currentTimeMillis(),
                    actor);
            handler.handle(context);
        }
    }

    /**
     * 包装被拦截的方法体。
     */
    @FunctionalInterface
    public interface Callback {

        /**
         * 调用被拦截的方法。
         *
         * @return 方法执行结果
         * @throws Throwable 方法抛出时的结果
         */
        Object call() throws Throwable;
    }
}

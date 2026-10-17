package com.innospots.nexus.console.logger.domain.context;

/**
 * 单次被拦截调用的框架无关描述。
 * <p>由拦截器适配器在 {@code @AuditLog} 标注方法周围组装，并交给
 * {@link com.innospots.nexus.console.logger.InvocationLogHandler} 用于持久化。
 * 不含框架类型，任意 Java 运行时均可产生并消费。本记录是从拦截器边界传入
 * 审计日志操作器的标准领域上下文。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @param className  被拦截方法的声明类名
 * @param methodName 被拦截的方法名
 * @param action     由 {@link com.innospots.nexus.console.logger.AuditLog} 注解声明的业务动作编码
 * @param arguments  捕获的方法参数；未记录时为空数组
 * @param result     捕获的返回值；未记录或失败时为 null
 * @param exception  抛出的异常；成功时为 null
 * @param startTime  调用开始时间（epoch 毫秒）
 * @param endTime    调用结束时间（epoch 毫秒）
 * @param actor            操作用户身份；未知时为空字符串
 * @param operationMessage 注解声明的操作描述（当前语言环境解析后）
 * @param httpPath         HTTP 方法 + 路径；非 HTTP 拦截时可为空
 */
public record InvocationLogContext(
        String className,
        String methodName,
        String action,
        Object[] arguments,
        Object result,
        Throwable exception,
        long startTime,
        long endTime,
        String actor,
        String operationMessage,
        String httpPath
) {

    public InvocationLogContext(
            String className,
            String methodName,
            String action,
            Object[] arguments,
            Object result,
            Throwable exception,
            long startTime,
            long endTime,
            String actor
    ) {
        this(
                className,
                methodName,
                action,
                arguments,
                result,
                exception,
                startTime,
                endTime,
                actor,
                "",
                "");
    }

    public InvocationLogContext {
        className = className == null ? "" : className;
        methodName = methodName == null ? "" : methodName;
        action = action == null ? "" : action;
        arguments = arguments == null ? new Object[0] : arguments.clone();
        actor = actor == null ? "" : actor;
        operationMessage = operationMessage == null ? "" : operationMessage;
        httpPath = httpPath == null ? "" : httpPath;
    }

    /**
     * 执行耗时（毫秒）。
     *
     * @return endTime endTime 减 startTime
     */
    public long elapsedMillis() {
        return endTime - startTime;
    }

    /**
     * 调用是否未抛异常完成。
     *
     * @return true 未捕获异常时为 true
     */
    public boolean success() {
        return exception == null;
    }
}

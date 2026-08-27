package com.innospots.nexus.console.permission.authorization;

import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;

/** 通过线程上下文向数据访问适配器传递鉴权结果。 */
public final class AuthorizationScope implements AutoCloseable {

    private static final ThreadLocal<AuthorizationContext> CURRENT = new ThreadLocal<>();

    private final AuthorizationContext previous;
    private boolean closed;

    private AuthorizationScope(AuthorizationContext previous) {
        this.previous = previous;
    }

    /**
     * 为当前请求打开鉴权作用域。
     *
     * @param context 已通过鉴权器校验的鉴权上下文
     * @return 可用于 try-with-resources 的作用域
     * @throws NexusException 上下文为空时抛出参数异常
     */
    public static AuthorizationScope open(AuthorizationContext context) {
        if (context == null) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "Authorization context is required");
        }
        AuthorizationContext previous = CURRENT.get();
        CURRENT.set(context);
        return new AuthorizationScope(previous);
    }

    /**
     * 返回当前线程中的请求鉴权上下文。
     *
     * @return 已打开的上下文；当前线程没有鉴权作用域时为空
     */
    public static Optional<AuthorizationContext> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * 恢复嵌套作用域的上一级上下文，并清理最外层线程变量。
     * 重复关闭不会产生副作用。
     */
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            // 作用域可能嵌套，关闭时必须恢复父作用域，最外层才移除线程变量。
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }
}

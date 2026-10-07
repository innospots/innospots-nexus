package com.innospots.nexus.service.runtime.invocation;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * 单次在途调用跟踪对象。
 *
 * @author Smars
 * @date 2026/09/15
 * @see InvocationEngine
 * @see ServiceRuntime
 */
public final class InvocationCall {

    private final InvocationContext context;
    private final InvocationControl control;
    private final Instant startedAt;

    private InvocationCall(InvocationContext context, InvocationControl control, Instant startedAt) {
        this.context = context;
        this.control = control;
        this.startedAt = startedAt;
    }

    /**
     * 打开新的在途调用。
     *
     * @param context 调用上下文
     * @return 调用跟踪对象
     */
    public static InvocationCall open(InvocationContext context) {
        Checks.notNull(context, "context");
        return new InvocationCall(context, new InvocationControl(), Instant.now());
    }

    /**
     * 返回调用上下文。
     *
     * @return 上下文
     */
    public InvocationContext context() {
        return context;
    }

    /**
     * 返回调用控制面。
     *
     * @return 控制面
     */
    public InvocationControl control() {
        return control;
    }

    /**
     * 返回开始时间。
     *
     * @return 开始时间
     */
    public Instant startedAt() {
        return startedAt;
    }
}

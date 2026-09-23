package com.innospots.nexus.service.runtime.context;

import java.util.concurrent.Executor;

import com.innospots.nexus.base.util.Checks;

/**
 * 传播已捕获 {@link com.innospots.nexus.service.contract.context.ServiceContext} 的执行器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ContextPropagation
 * @see ThreadBoundServiceContext
 */
public final class ContextExecutor implements Executor {

    private final Executor delegate;
    private final ContextPropagation propagation;

    /**
     * 创建以 {@code propagation} 包装任务的执行器。
     *
     * @param delegate    底层执行器
     * @param propagation 上下文传播器
     */
    public ContextExecutor(Executor delegate, ContextPropagation propagation) {
        this.delegate = Checks.notNull(delegate, "delegate");
        this.propagation = Checks.notNull(propagation, "propagation");
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(propagation.wrap(Checks.notNull(command, "command")));
    }
}

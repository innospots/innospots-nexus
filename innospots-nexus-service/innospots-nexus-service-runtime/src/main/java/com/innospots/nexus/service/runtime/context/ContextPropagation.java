package com.innospots.nexus.service.runtime.context;

import java.util.concurrent.Callable;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * 捕获当前 {@link ServiceContext} 并在委派工作中重新安装。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ThreadBoundServiceContext
 * @see ContextExecutor
 */
public final class ContextPropagation {

    private final ThreadBoundServiceContext contexts;

    /**
     * 创建绑定到 {@code contexts} 的传播器。
     *
     * @param contexts 线程绑定访问器
     */
    public ContextPropagation(ThreadBoundServiceContext contexts) {
        this.contexts = Checks.notNull(contexts, "contexts");
    }

    /**
     * 包装 {@code runnable}，在运行期间安装已捕获上下文。
     *
     * @param runnable 待包装工作
     * @return 包装后的 Runnable
     */
    public Runnable wrap(Runnable runnable) {
        Checks.notNull(runnable, "runnable");
        ServiceContext captured = contexts.current().orElse(null);
        return () -> runWith(captured, runnable);
    }

    /**
     * 包装 {@code callable}，在调用期间安装已捕获上下文。
     *
     * @param callable 待包装工作
     * @param <V>      结果类型
     * @return 包装后的 Callable
     */
    public <V> Callable<V> wrap(Callable<V> callable) {
        Checks.notNull(callable, "callable");
        ServiceContext captured = contexts.current().orElse(null);
        return () -> {
            ContextSnapshot snapshot = captured == null ? null : contexts.install(captured);
            try {
                return callable.call();
            } finally {
                restore(snapshot);
            }
        };
    }

    private void runWith(ServiceContext captured, Runnable runnable) {
        ContextSnapshot snapshot = captured == null ? null : contexts.install(captured);
        try {
            runnable.run();
        } finally {
            restore(snapshot);
        }
    }

    private void restore(ContextSnapshot snapshot) {
        if (snapshot != null) {
            contexts.restore(snapshot);
        }
    }
}

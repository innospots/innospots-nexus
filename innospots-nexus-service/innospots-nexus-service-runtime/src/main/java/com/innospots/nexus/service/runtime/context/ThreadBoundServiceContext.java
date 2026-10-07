package com.innospots.nexus.service.runtime.context;

import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;

/**
 * 线程本地 {@link ServiceContext} 访问器。恢复时不会清除外层嵌套上下文。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ContextSnapshot
 * @see ServiceContextAccessor
 */
public final class ThreadBoundServiceContext implements ServiceContextAccessor {

    private final ThreadLocal<ServiceContext> current = new ThreadLocal<>();

    @Override
    public Optional<ServiceContext> current() {
        return Optional.ofNullable(current.get());
    }

    /**
     * 在当前线程安装 {@code context} 并返回先前快照。
     *
     * @param context 待安装上下文
     * @return 先前绑定的快照
     */
    public ContextSnapshot install(ServiceContext context) {
        Checks.notNull(context, "context");
        ServiceContext previous = current.get();
        current.set(context);
        return new ContextSnapshot(previous);
    }

    /**
     * 恢复先前绑定。{@code null} 先前值将移除线程本地变量。
     *
     * @param snapshot 来自 {@link #install(ServiceContext)} 的快照
     */
    public void restore(ContextSnapshot snapshot) {
        Checks.notNull(snapshot, "snapshot");
        if (snapshot.previous() == null) {
            current.remove();
        } else {
            current.set(snapshot.previous());
        }
    }
}

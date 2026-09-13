package com.innospots.nexus.service.runtime.context;

import java.util.Optional;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContext;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;

/**
 * Thread-local {@link ServiceContext} accessor. Restore never clears an outer nested context.
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
     * Installs {@code context} on the current thread and returns the previous snapshot.
     *
     * @param context context to install
     * @return snapshot of the previous binding
     */
    public ContextSnapshot install(ServiceContext context) {
        Checks.notNull(context, "context");
        ServiceContext previous = current.get();
        current.set(context);
        return new ContextSnapshot(previous);
    }

    /**
     * Restores the previous binding. {@code null} previous removes the thread local.
     *
     * @param snapshot snapshot from {@link #install(ServiceContext)}
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

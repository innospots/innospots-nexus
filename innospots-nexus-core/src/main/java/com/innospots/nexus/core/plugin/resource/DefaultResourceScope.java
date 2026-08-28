package com.innospots.nexus.core.plugin.resource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * Thread-safe resource stack that executes every disposer once in reverse registration order.
 */
public final class DefaultResourceScope implements ResourceScope {

    private final Deque<Registration> registrations = new ArrayDeque<>();
    private boolean closed;

    @Override
    public <T extends AutoCloseable> T manage(T resource) {
        if (resource == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_START_FAILED, "managed resource must not be null");
        }
        try {
            add(() -> closeResource(resource));
            return resource;
        } catch (RuntimeException registrationFailure) {
            try {
                closeResource(resource);
            } catch (RuntimeException closeFailure) {
                registrationFailure.addSuppressed(closeFailure);
            }
            throw registrationFailure;
        }
    }

    @Override
    public synchronized ResourceRegistration add(Runnable disposer) {
        if (disposer == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_START_FAILED, "resource disposer must not be null");
        }
        if (closed) {
            throw NexusException.build(PluginStatusCode.PLUGIN_START_FAILED, "resource scope is already closed");
        }
        Registration registration = new Registration(disposer);
        registrations.push(registration);
        return registration;
    }

    @Override
    public void close() {
        List<Registration> closing;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            closing = new ArrayList<>(registrations);
            registrations.clear();
        }
        Throwable first = null;
        for (Registration registration : closing) {
            try {
                registration.close();
            } catch (Throwable exception) {
                if (first == null) {
                    first = exception;
                } else {
                    first.addSuppressed(exception);
                }
            }
        }
        if (first != null) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(),
                    "failed to close plugin resources",
                    first);
        }
    }

    private static void closeResource(AutoCloseable resource) {
        try {
            resource.close();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_STOP_FAILED.fullCode(),
                    "failed to close managed resource",
                    exception);
        }
    }

    private static final class Registration implements ResourceRegistration {

        private final Runnable disposer;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Registration(Runnable disposer) {
            this.disposer = disposer;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                disposer.run();
            }
        }
    }
}

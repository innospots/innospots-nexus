package com.innospots.nexus.core.plugin.resource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程安全的资源栈，按注册逆序执行每个释放器且每个释放器最多执行一次。
 */
public final class DefaultResourceScope implements ResourceScope {

    private static final Logger logger = LoggerFactory.getLogger(DefaultResourceScope.class);

    private final Deque<Registration> registrations = new ArrayDeque<>();
    private boolean closed;

    /**
     * 注册一个托管资源，作用域关闭时按注册逆序释放。
     *
     * @param resource 要托管的自动关闭资源
     * @param <T> 资源类型
     * @return 传入的资源实例
     * @throws NexusException 资源为空或作用域已关闭时抛出
     */
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

    /**
     * 注册一个自定义释放器，作用域关闭时按注册逆序执行。
     *
     * @param disposer 资源释放逻辑
     * @return 可单独关闭的注册句柄
     * @throws NexusException 释放器为空或作用域已关闭时抛出
     */
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

    /**
     * 按注册逆序释放全部资源；重复调用幂等。
     *
     * @throws NexusException 任一释放器失败时抛出，并抑制后续失败
     */
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
        if (closing.isEmpty()) {
            logger.debug("Resource scope closed with no managed resources");
            return;
        }
        logger.debug("Closing resource scope with {} managed resource(s)", closing.size());
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
            logger.warn("Failed to close {} managed resource(s)", closing.size(), first);
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

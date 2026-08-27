package com.innospots.nexus.console.logger;

import java.util.Objects;

import com.innospots.nexus.console.logger.domain.context.InvocationLogContext;

/**
 * Reusable interception routine shared by all framework adapters.
 * <p>Wraps a {@link Callback} with timing, outcome capture, and delegation to
 * an {@link InvocationLogHandler}. Framework adapters (AspectJ, Byte Buddy,
 * CDI, ...) call {@link #execute} around annotated methods; this class never
 * depends on any interception framework.</p>
 */
public final class LogExecutor {

    private final InvocationLogHandler handler;

    /**
     * Creates an executor delegating to the given handler.
     *
     * @param handler target handler, must not be null
     */
    public LogExecutor(InvocationLogHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler must not be null");
    }

    /**
     * Runs the callback and records an audit context for the invocation.
     * <p>The callback outcome is rethrown unchanged; the audit record is
     * always produced through the configured handler, even on failure.</p>
     *
     * @param auditLog   annotation declaring what to capture
     * @param className  declaring class name of the intercepted method
     * @param methodName intercepted method name
     * @param actor      identity of the acting user, or null when unknown
     * @param arguments  intercepted method arguments, or null
     * @param callback   the actual method invocation
     * @return the callback result
     * @throws Throwable rethrows the callback outcome unchanged
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
     * Wraps the intercepted method body.
     */
    @FunctionalInterface
    public interface Callback {

        /**
         * Invokes the intercepted method.
         *
         * @return the method result
         * @throws Throwable the method outcome when it throws
         */
        Object call() throws Throwable;
    }
}

package com.innospots.nexus.kernel.logger.domain.context;

/**
 * Framework-independent description of a single intercepted invocation.
 * <p>Assembled by an interceptor adapter around an {@code @AuditLog}-annotated
 * method and handed to an {@link com.innospots.nexus.kernel.logger.InvocationLogHandler}
 * for persistence. Contains no framework type so any Java runtime can produce
 * and consume it. This record is the canonical domain context carried from the
 * interceptor boundary into the audit log operator.</p>
 *
 * @param className  declaring class name of the intercepted method
 * @param methodName intercepted method name
 * @param action     business action code declared by the
 *                   {@link com.innospots.nexus.kernel.logger.AuditLog} annotation
 * @param arguments  captured method arguments, or an empty array when not recorded
 * @param result     captured return value, or null when not recorded or on failure
 * @param exception  thrown exception, or null on success
 * @param startTime  invocation start time in epoch milliseconds
 * @param endTime    invocation end time in epoch milliseconds
 * @param actor      identity of the acting user, or an empty string when unknown
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
        String actor
) {

    public InvocationLogContext {
        className = className == null ? "" : className;
        methodName = methodName == null ? "" : methodName;
        action = action == null ? "" : action;
        arguments = arguments == null ? new Object[0] : arguments.clone();
        actor = actor == null ? "" : actor;
    }

    /**
     * Elapsed execution time in milliseconds.
     *
     * @return endTime minus startTime
     */
    public long elapsedMillis() {
        return endTime - startTime;
    }

    /**
     * Whether the invocation completed without throwing.
     *
     * @return true when no exception was captured
     */
    public boolean success() {
        return exception == null;
    }
}

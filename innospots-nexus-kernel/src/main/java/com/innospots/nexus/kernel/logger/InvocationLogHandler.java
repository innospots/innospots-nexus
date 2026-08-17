package com.innospots.nexus.kernel.logger;

import com.innospots.nexus.kernel.logger.domain.context.InvocationLogContext;

/**
 * Common interception handler port.
 * <p>Receives a fully assembled {@link InvocationLogContext} and is responsible
 * for persisting or forwarding it. Implementations remain framework-independent
 * and may, for example, write an {@code AuditLogEntity} through
 * {@code AuditLogDao} or publish the context to another sink.</p>
 */
@FunctionalInterface
public interface InvocationLogHandler {

    /**
     * Handles a completed invocation context.
     *
     * @param context assembled invocation data, never null
     */
    void handle(InvocationLogContext context);
}

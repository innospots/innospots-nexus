package com.innospots.nexus.service.contract.cancellation;

/**
 * Registration returned by {@link CancellationToken#onCancel(java.util.function.Consumer)}.
 * Closing is idempotent.
 *
 * @author Smars
 * @date 2026/09/13
 * @see CancellationToken
 */
@FunctionalInterface
public interface CancellationRegistration extends AutoCloseable {

    /**
     * Unregisters the listener. Repeated closes have no effect.
     */
    @Override
    void close();
}

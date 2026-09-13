package com.innospots.nexus.service.contract.context;

import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * Host-injected accessor for the current {@link ServiceContext}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see ServiceContext
 */
@FunctionalInterface
public interface ServiceContextAccessor {

    /**
     * Returns the current context when one is bound.
     *
     * @return current context or empty
     */
    Optional<ServiceContext> current();

    /**
     * Returns the current context or fails when none is bound.
     *
     * @return current context
     * @throws NexusException when no context is available
     */
    default ServiceContext requireCurrent() {
        return current().orElseThrow(() -> NexusException.build(ServiceStatusCode.CONTEXT_UNAVAILABLE));
    }
}

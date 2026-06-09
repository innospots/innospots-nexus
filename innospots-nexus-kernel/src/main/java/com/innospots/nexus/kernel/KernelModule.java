package com.innospots.nexus.kernel;

import java.util.Set;

import com.innospots.nexus.console.api.ConsoleEndpoint;
import com.innospots.nexus.console.extension.ConsoleExtension;
import com.innospots.nexus.kernel.domain.KernelDomain;

/**
 * Functional kernel module marker.
 * <p>
 * The kernel builds on console-facing contracts while keeping concrete runtime
 * implementations outside this foundation module.
 * </p>
 */
public class KernelModule {

    private static final Set<KernelDomain> DOMAINS = Set.of(KernelDomain.values());

    /**
     * Exposes the console boundary type to make the module dependency explicit
     * and testable without instantiating a web framework.
     *
     * @return console JAX-RS endpoint contract type
     */
    public Class<ConsoleEndpoint> consoleEndpointType() {
        return ConsoleEndpoint.class;
    }

    /**
     * Exposes the console extension contract used by kernel capabilities.
     *
     * @return console extension contract type
     */
    public Class<ConsoleExtension> consoleExtensionType() {
        return ConsoleExtension.class;
    }

    /**
     * Returns the core business domains owned by the kernel module.
     *
     * @return immutable kernel domain set
     */
    public Set<KernelDomain> domains() {
        return DOMAINS;
    }
}

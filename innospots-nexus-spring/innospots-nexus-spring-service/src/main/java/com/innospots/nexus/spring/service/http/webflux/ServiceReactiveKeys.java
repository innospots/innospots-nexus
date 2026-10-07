package com.innospots.nexus.spring.service.http.webflux;

import com.innospots.nexus.service.contract.context.ServiceContext;

/**
 * Reactor Context 键名。
 */
public final class ServiceReactiveKeys {

    /**
     * {@link ServiceContext} 在 Reactor Context 中的键。
     */
    public static final String SERVICE_CONTEXT = "nexus.service.context";

    private ServiceReactiveKeys() {
    }
}

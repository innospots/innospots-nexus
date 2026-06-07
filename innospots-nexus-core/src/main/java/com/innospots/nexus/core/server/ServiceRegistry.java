package com.innospots.nexus.core.server;

import java.util.List;

public interface ServiceRegistry {

/**
     * Register or update a service node in the registry.
     *
     * @param service the service info to register
     */
    void register(ServiceInfo service);

    /**
     * Remove a service node from the registry by its server key.
     *
     * @param serverKey the unique key (host:port) identifying the service
     */
    void unregister(String serverKey);

    /**
     * Find a service by its server key (host:port).
     *
     * @param serverKey the unique key to look up
     * @return the matching service info, or null if not found
     */
    ServiceInfo findByKey(String serverKey);

    /**
     * List all online services.
     *
     * @return list of services with ONLINE status
     */
    List<ServiceInfo> listOnline();

    /**
     * List all registered services regardless of status.
     *
     * @return full list of registered services
     */
    List<ServiceInfo> listAll();

    /**
     * Count of currently online services.
     *
     * @return number of services with ONLINE status
     */
    int onlineCount();
}

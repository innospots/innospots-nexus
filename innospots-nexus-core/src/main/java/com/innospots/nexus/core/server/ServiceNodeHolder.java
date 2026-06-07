package com.innospots.nexus.core.server;

import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Holds the local node's service registration state and provides
 * leader detection, heartbeat invalidation, and shard computation.
 * <p>All state fields are {@code volatile} for visibility across threads.</p>
 */
public class ServiceNodeHolder {

    private final long maxValidSeconds;
    private final ServiceLifecycle lifecycle = new ServiceLifecycle();

    private volatile ServiceInfo currentService;
    private volatile LocalDateTime startupTime;
    private volatile boolean leader;
    @Setter
    private volatile int position = -1;
    @Setter
    private volatile int availableServicesSize;

    /**
     * @param maxValidSeconds max seconds since last heartbeat before a node is considered invalid
     */
    public ServiceNodeHolder(long maxValidSeconds) {
        this.maxValidSeconds = maxValidSeconds;
    }

    /** Returns the mutable lifecycle state for the local node. */
    public ServiceLifecycle lifecycle() {
        return lifecycle;
    }

    /** Records the current time as the node startup instant. */
    public void markStartup() {
        startupTime = LocalDateTime.now();
    }

    /** Returns the recorded startup time, or null if not yet marked. */
    public LocalDateTime startupTime() {
        return startupTime;
    }

    /**
     * Register or update the current node's service info.
     * Leader status is derived from the service role.
     */
    public void register(ServiceInfo service) {
        this.currentService = service;
        this.leader = service.role() == ServiceRole.LEADER;
        lifecycle.setServerKey(service.serverKey());
        lifecycle.setLeader(this.leader);
    }

    /** Unregister the current node and reset leader state. */
    public void unregister() {
        this.currentService = null;
        this.leader = false;
        lifecycle.setServerKey(null);
        lifecycle.setLeader(false);
    }

    /** Returns true if a service has been registered on this node. */
    public boolean isRegistered() {
        return currentService != null;
    }

    /** Returns the current service info, or null if not registered. */
    public ServiceInfo currentService() {
        return currentService;
    }

    /**
     * Current node is leader AND heartbeat is still within the valid window.
     *
     * @return true if the node is the active leader
     */
    public boolean isLeader() {
        return leader
                && currentService != null
                && currentService.elapsedSecondsSinceUpdate() < maxValidSeconds;
    }

    /**
     * A remote service is considered invalid when its heartbeat has
     * exceeded {@code maxValidSeconds}.
     */
    public boolean isInvalid(ServiceInfo service) {
        return service != null && service.elapsedSecondsSinceUpdate() > maxValidSeconds;
    }

    /** Returns the node position in the cluster (0-based), or -1 if unset. */
    public int position() {
        return position;
    }

    /** Returns the total number of available services in the cluster. */
    public int availableServicesSize() {
        return availableServicesSize;
    }

    /**
     * Computes the range of sharding keys assigned to this node based
     * on its position in the cluster, using ceiling division.
     *
     * @param totalKeys total number of shard keys
     * @return array of key indices, or empty if position is not set or total is zero
     */
    public int[] computeShardingKeys(int totalKeys) {
        if (position < 0 || availableServicesSize <= 0 || totalKeys <= 0) {
            return new int[0];
        }
        int perNode = (int) Math.ceil((double) totalKeys / availableServicesSize);
        int start = position * perNode;
        int end = Math.min((position + 1) * perNode, totalKeys);
        if (start >= totalKeys) {
            return new int[0];
        }
        int[] keys = new int[end - start];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = start + i;
        }
        return keys;
    }
}

package com.innospots.nexus.core.server;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fluent domain model for a registered service node.
 * <p>Carries identity (service name + instance ID), network address,
 * role/status, group membership, free-form tags, and runtime metrics.
 * The {@link #serverKey()} is derived as {@code host:port} for
 * cluster-wide uniqueness.</p>
 */
public class ServiceInfo {

    private Long serverId;
    private final String serviceName;
    private final String instanceId;
    private String host;
    private int port;
    private ServiceStatus status = ServiceStatus.ONLINE;
    private ServiceRole role = ServiceRole.FOLLOWER;
    private String group = "default";
    private LocalDateTime updatedTime = LocalDateTime.now();
    private final Map<String, String> tags = new LinkedHashMap<>();
    private final Map<String, String> metrics = new LinkedHashMap<>();

    private ServiceInfo(String serviceName, String instanceId) {
        this.serviceName = serviceName;
        this.instanceId = instanceId;
    }

    /**
     * Creates a service with the given name and instance identifier.
     *
     * @param serviceName the logical service type name
     * @param instanceId  unique instance identifier (e.g. UUID or hostname)
     */
    public static ServiceInfo named(String serviceName, String instanceId) {
        return new ServiceInfo(serviceName, instanceId);
    }

    /** Returns the database-assigned server identifier. */
    public Long serverId() {
        return serverId;
    }

    /** Sets the database-assigned server identifier and returns this for chaining. */
    public ServiceInfo serverId(Long serverId) {
        this.serverId = serverId;
        return this;
    }

    /** Returns the logical service type name. */
    public String serviceName() {
        return serviceName;
    }

    /** Returns the unique instance identifier. */
    public String instanceId() {
        return instanceId;
    }

    /** Returns the host address. */
    public String host() {
        return host;
    }

    /** Sets the host address and returns this for chaining. */
    public ServiceInfo host(String host) {
        this.host = host;
        return this;
    }

    /** Returns the port number. */
    public int port() {
        return port;
    }

    /** Sets the port number and returns this for chaining. */
    public ServiceInfo port(int port) {
        this.port = port;
        return this;
    }

    /** Returns the service status. */
    public ServiceStatus status() {
        return status;
    }

    /** Sets the service status (defaults to ONLINE if null) and returns this for chaining. */
    public ServiceInfo status(ServiceStatus status) {
        this.status = status == null ? ServiceStatus.ONLINE : status;
        return this;
    }

    /** Returns the service role. */
    public ServiceRole role() {
        return role;
    }

    /** Sets the service role (defaults to FOLLOWER if null) and returns this for chaining. */
    public ServiceInfo role(ServiceRole role) {
        this.role = role == null ? ServiceRole.FOLLOWER : role;
        return this;
    }

    /** Returns the group name. */
    public String group() {
        return group;
    }

    /** Sets the group name (defaults to "default" if null) and returns this for chaining. */
    public ServiceInfo group(String group) {
        this.group = group == null ? "default" : group;
        return this;
    }

    /** Returns the last heartbeat update time. */
    public LocalDateTime updatedTime() {
        return updatedTime;
    }

    /** Sets the last heartbeat update time and returns this for chaining. */
    public ServiceInfo updatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

    /** Unique cluster-wide key: {@code host:port}. */
    public String serverKey() {
        return host + ":" + port;
    }

    /** Returns an immutable copy of the tags map. */
    public Map<String, String> tags() {
        return Map.copyOf(tags);
    }

    /**
     * Parses and sets tags from a comma-separated string of {@code key=value} pairs.
     * Example: {@code "env=prod,region=us-east-1"}.
     */
    public ServiceInfo tags(String tags) {
        if (tags != null) {
            for (String pair : tags.split(",")) {
                String[] kv = pair.trim().split("=", 2);
                if (kv.length == 2) {
                    this.tags.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        return this;
    }

    /** Returns an immutable copy of the metrics map. */
    public Map<String, String> metrics() {
        return Map.copyOf(metrics);
    }

    /** Merges the given map into the runtime metrics. */
    public ServiceInfo metrics(Map<String, String> metrics) {
        if (metrics != null) {
            this.metrics.putAll(metrics);
        }
        return this;
    }

    /** Seconds since the last heartbeat update. Used for expiry detection. */
    public long elapsedSecondsSinceUpdate() {
        return updatedTime == null
                ? 0
                : java.time.Duration.between(updatedTime, LocalDateTime.now()).toSeconds();
    }
}

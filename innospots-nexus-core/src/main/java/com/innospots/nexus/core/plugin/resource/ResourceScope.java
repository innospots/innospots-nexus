package com.innospots.nexus.core.plugin.resource;

/**
 * Per-start-cycle ownership boundary that releases registered resources in reverse order.
 */
public interface ResourceScope extends AutoCloseable {

    /**
     * Registers and returns an auto-closeable resource.
     *
     * @param resource resource owned by this start cycle
     * @param <T> resource type
     * @return the same resource for fluent use
     */
    <T extends AutoCloseable> T manage(T resource);

    /**
     * Registers a disposer for a resource without an AutoCloseable contract.
     *
     * @param disposer cleanup action invoked at most once
     * @return idempotent registration handle
     */
    ResourceRegistration add(Runnable disposer);

    /** Releases all registered resources. */
    @Override
    void close();
}

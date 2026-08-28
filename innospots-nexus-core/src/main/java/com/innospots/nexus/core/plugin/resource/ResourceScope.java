package com.innospots.nexus.core.plugin.resource;

/**
 * Per-start-cycle ownership boundary that releases registered resources in reverse order.
 */
public interface ResourceScope extends AutoCloseable {

    /** Registers and returns an auto-closeable resource. */
    <T extends AutoCloseable> T manage(T resource);

    /** Registers a disposer for a resource without an AutoCloseable contract. */
    ResourceRegistration add(Runnable disposer);

    /** Releases all registered resources. */
    @Override
    void close();
}

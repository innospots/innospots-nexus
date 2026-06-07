package com.innospots.nexus.core.server;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mutable lifecycle state for a local service node.
 * <p>Thread-safe via {@link AtomicBoolean} and {@code volatile} fields.
 * Tracks whether the node is running, its cluster key, and leader status.</p>
 */
public class ServiceLifecycle {

    private final AtomicBoolean running = new AtomicBoolean(true);
    @Getter
    @Setter
    private volatile String serverKey;
    @Setter
    private volatile boolean leader;

    /** Returns true while the service is running (not shut down). */
    public boolean isRunning() {
        return running.get();
    }

    /** Returns true after {@link #shutdown()} has been called. */
    public boolean isShutdown() {
        return !running.get();
    }

    /** Gracefully marks the service as stopped. */
    public void shutdown() {
        running.set(false);
    }

    /** Returns the cluster-wide server key ({@code host:port}). */
    public String serverKey() {
        return serverKey;
    }

    /** Returns true if this node is the current leader. */
    public boolean isLeader() {
        return leader;
    }

    /** True once a server key has been assigned via registration. */
    public boolean isRegistered() {
        return serverKey != null;
    }
}

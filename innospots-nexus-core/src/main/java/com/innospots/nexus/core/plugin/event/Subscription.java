package com.innospots.nexus.core.plugin.event;

/** An idempotent event subscription handle. */
public interface Subscription extends AutoCloseable {

    /** Removes the subscription at most once. */
    @Override
    void close();
}

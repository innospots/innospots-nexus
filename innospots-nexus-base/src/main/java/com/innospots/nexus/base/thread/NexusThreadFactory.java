package com.innospots.nexus.base.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Named {@link ThreadFactory} that produces threads with a configurable
 * prefix (default {@code nexus-worker}) and sequence number.
 */
public final class NexusThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final boolean daemon;
    private final AtomicInteger sequence = new AtomicInteger(1);

    public NexusThreadFactory(String namePrefix) {
        this(namePrefix, false);
    }

    public NexusThreadFactory(String namePrefix, boolean daemon) {
        this.namePrefix = namePrefix == null || namePrefix.isBlank() ? "nexus-worker" : namePrefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, namePrefix + "-" + sequence.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }
}

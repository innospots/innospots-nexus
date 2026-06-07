package com.innospots.nexus.core.watcher;

import com.innospots.nexus.base.thread.ThreadPoolBuilder;
import com.innospots.nexus.base.thread.NexusThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Supervisor that manages a pool of background {@link IWatcher} threads.
 * <p>Implements {@link AutoCloseable} for graceful shutdown via
 * try-with-resources.</p>
 */
public class WatcherSupervisor implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(WatcherSupervisor.class);

    private final int maxSize;
    private final String name;
    private final List<IWatcher> watchers;
    private NexusThreadPoolExecutor executor;

    /**
     * @param maxSize maximum concurrent watchers / thread pool size
     * @param name    pool name for thread naming
     */
    public WatcherSupervisor(int maxSize, String name) {
        this.maxSize = maxSize;
        this.name = name;
        this.watchers = new ArrayList<>(maxSize);
        this.executor = ThreadPoolBuilder.build(maxSize, maxSize, 0, name);
    }

    /** Creates a supervisor with the default name {@code watcher-supervisor}. */
    public WatcherSupervisor(int maxSize) {
        this(maxSize, "watcher-supervisor");
    }

    /**
     * Registers and starts a watcher in the thread pool.
     */
    public void register(IWatcher watcher) {
        logger.info("Register watcher: {}", watcher.getClass().getSimpleName());
        if (executor == null) {
            executor = ThreadPoolBuilder.build(maxSize, maxSize, 0, name);
        }
        executor.submit(watcher);
        watchers.add(watcher);
    }

    /** Returns the number of currently registered watchers. */
    public int activeCount() {
        return watchers.size();
    }

    /**
     * Graceful shutdown: stops all watchers, waits 1s, then shuts down the pool.
     */
    @Override
    public void close() {
        for (IWatcher watcher : watchers) {
            watcher.stop();
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        watchers.clear();
    }
}

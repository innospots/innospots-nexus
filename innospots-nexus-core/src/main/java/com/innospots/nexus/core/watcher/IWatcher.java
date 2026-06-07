package com.innospots.nexus.core.watcher;

/**
 * Background watcher interface. Extends {@link Runnable} for execution
 * in a thread pool. The lifecycle is: {@link #check()} → {@link #execute()}
 * in a loop until stopped.
 *
 * @see AbstractWatcher
 * @see WatcherSupervisor
 */
public interface IWatcher extends Runnable {

    /** Human-readable watcher name for logging. */
    String name();

    /** Whether the watcher is currently executing its loop. */
    boolean isRunning();

    /**
     * Pre-execution check. Return false to skip this cycle.
     * Default implementation always returns true.
     */
    default boolean check() {
        return true;
    }

    /**
     * Execute one cycle of work. Returns the interval (ms) before the next
     * cycle. A value &le; 0 falls back to the default interval.
     */
    int execute();

    /** Request the watcher to stop after the current cycle. */
    void stop();
}

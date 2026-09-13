package com.innospots.nexus.service.contract.time;

/**
 * Monotonic nanosecond source used by {@link Deadline}.
 *
 * @author Smars
 * @date 2026/09/13
 * @see Deadline
 */
@FunctionalInterface
public interface Ticker {

    /**
     * Returns a monotonic timestamp in nanoseconds.
     *
     * @return nanoseconds
     */
    long readNanos();

    /**
     * Returns a ticker backed by {@link System#nanoTime()}.
     *
     * @return system ticker
     */
    static Ticker system() {
        return System::nanoTime;
    }
}

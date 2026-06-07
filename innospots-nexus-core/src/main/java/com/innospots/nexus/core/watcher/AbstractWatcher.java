package com.innospots.nexus.core.watcher;

import com.innospots.nexus.base.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Template-method base class for background watchers.
 * <p>Runs a loop: {@link #check()} → {@link #execute()} → sleep, until
 * {@link #stop()} is called or the {@code runningCondition} supplier
 * returns false. The actual work is defined by subclasses via
 * {@link #execute()}.</p>
 */
public abstract class AbstractWatcher implements IWatcher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWatcher.class);

    private final String name;
    private final int checkIntervalMillis;
    private final Supplier<Boolean> runningCondition;
    private volatile boolean running;
    private long startTimeMillis;

    /**
     * @param name                watcher name for logging
     * @param checkIntervalMillis sleep interval between cycles
     * @param runningCondition    checked each cycle; loop exits when false
     */
    protected AbstractWatcher(String name, int checkIntervalMillis, Supplier<Boolean> runningCondition) {
        this.name = name;
        this.checkIntervalMillis = checkIntervalMillis;
        this.runningCondition = runningCondition;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Main execution loop: check → execute → sleep, repeating until
     * stopped or the running condition returns false.
     */
    @Override
    public void run() {
        running = true;
        startTimeMillis = System.currentTimeMillis();
        logger.info("Watcher started: {}", name);

        int interval = checkIntervalMillis;
        while (running && runningCondition.get()) {
            try {
                if (check()) {
                    interval = execute();
                }
                // Fall back to default interval if execute() returns none
                if (interval <= 0) {
                    interval = checkIntervalMillis;
                }
            } catch (Exception e) {
                logger.error("Watcher {} execution error: {}", name, e.getMessage(), e);
            } finally {
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error("Watcher {} interrupted", name, e);
                    Thread.currentThread().interrupt();
                    running = false;
                }
            }
            // Re-check condition after sleep in case it changed
            running = runningCondition.get();
        }

        running = false;
        logger.info("Watcher {} stopped, uptime: {}", name, DateTimeUtils.consume(startTimeMillis));
    }

    /** Sets the running flag to false; the loop will exit after the current cycle. */
    @Override
    public void stop() {
        running = false;
    }
}

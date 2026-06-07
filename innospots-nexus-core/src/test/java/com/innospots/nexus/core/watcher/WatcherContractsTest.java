package com.innospots.nexus.core.watcher;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class WatcherContractsTest {

    @Test
    void watcherLifecycleInvokesLoopUntilStopped() {
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean running = new AtomicBoolean(true);

        IWatcher watcher = new AbstractWatcher("test-watcher", 100, running::get) {
            @Override
            public int execute() {
                count.incrementAndGet();
                if (count.get() >= 3) {
                    running.set(false);
                }
                return 100;
            }
        };

        watcher.run();

        assertThat(count.get()).isEqualTo(3);
        assertThat(watcher.name()).isEqualTo("test-watcher");
        assertThat(watcher.isRunning()).isFalse();
    }

    @Test
    void supervisorManagesWatcherPool() {
        AtomicInteger count = new AtomicInteger(0);
        AtomicBoolean supervisorRunning = new AtomicBoolean(true);

        WatcherSupervisor supervisor = new WatcherSupervisor(2, "test-supervisor");

        IWatcher watcher = new AbstractWatcher("counter", 50, supervisorRunning::get) {
            @Override
            public int execute() {
                count.incrementAndGet();
                if (count.get() >= 3) {
                    supervisorRunning.set(false);
                }
                return 50;
            }
        };

        supervisor.register(watcher);
        assertThat(supervisor.activeCount()).isEqualTo(1);
    }
}

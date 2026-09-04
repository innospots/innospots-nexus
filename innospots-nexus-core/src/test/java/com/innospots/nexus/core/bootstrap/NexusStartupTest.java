package com.innospots.nexus.core.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NexusStartupTest {

    @Test
    void runsTasksInOrderAndStopsOnFailure() {
        List<String> executed = new ArrayList<>();

        NexusStartup startup = NexusStartup.builder()
                .task(recordingTask("catalog-sync", 200, executed))
                .task(recordingTask("plugin-host", 100, executed))
                .task(failingTask("seed", 300))
                .build();

        assertThatThrownBy(startup::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("seed failed");

        assertThat(executed).containsExactly("plugin-host", "catalog-sync");
    }

    @Test
    void requiresAtLeastOneTask() {
        assertThatThrownBy(() -> NexusStartup.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("at least one startup task is required");
    }

    private static NexusStartupTask recordingTask(String name, int order, List<String> executed) {
        return new NexusStartupTask() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public void run(NexusStartupContext context) {
                executed.add(name);
            }
        };
    }

    private static NexusStartupTask failingTask(String name, int order) {
        return new NexusStartupTask() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public int order() {
                return order;
            }

            @Override
            public void run(NexusStartupContext context) {
                throw new IllegalStateException(name + " failed");
            }
        };
    }
}

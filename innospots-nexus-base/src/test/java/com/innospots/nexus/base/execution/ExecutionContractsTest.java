package com.innospots.nexus.base.execution;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionContractsTest {

    @Test
    void definesExecutionLifecycleStatuses() {
        assertThat(ExecutionStatus.values()).contains(
                ExecutionStatus.CREATED,
                ExecutionStatus.STARTING,
                ExecutionStatus.READY,
                ExecutionStatus.PENDING,
                ExecutionStatus.RUNNING,
                ExecutionStatus.STOPPING,
                ExecutionStatus.STOPPED,
                ExecutionStatus.SUCCESS,
                ExecutionStatus.FAILED
        );
    }

    @Test
    void contextCarriesInputValuesForExecutor() {
        ExecutionContext context = ExecutionContext.create("exec-1")
                .input("name", "nexus")
                .input("count", "3")
                .put("traceId", "trace-1");

        Executor<String, ExecutionContext> executor = new Executor<>() {
            @Override
            public String identifier() {
                return "demo";
            }

            @Override
            public String execute(ExecutionContext context) {
                return context.getInputString("name") + "-" + context.getInputInteger("count");
            }
        };

        assertThat(executor.execute(context)).isEqualTo("nexus-3");
        assertThat(executor.info()).isEqualTo("demo");
        assertThat(context.getString("traceId")).isEqualTo("trace-1");
        assertThat(context.context()).containsEntry("traceId", "trace-1");
    }

    @Test
    void recordAndContextCanBeExtendedByPlatformModules() {
        PluginExecutionContext context = new PluginExecutionContext("exec-2").pluginId("plugin-a");
        context.input("payload", Map.of("value", 7));

        PluginExecutionRecord record = new PluginExecutionRecord(
                context.executionId(),
                "plugin-executor",
                ExecutionStatus.SUCCESS,
                LocalDateTime.now(),
                LocalDateTime.now(),
                context.context(),
                Map.of("accepted", true),
                "done",
                context.pluginId()
        );

        assertThat(context.pluginId()).isEqualTo("plugin-a");
        assertThat(context.getInput("payload")).isEqualTo(Map.of("value", 7));
        assertThat(record.context()).isEqualTo(context.context());
        assertThat(record.pluginId()).isEqualTo("plugin-a");
        assertThat(record.output()).containsEntry("accepted", true);
    }

    private static final class PluginExecutionContext extends ExecutionContext {

        private String pluginId;

        private PluginExecutionContext(String executionId) {
            super(executionId);
        }

        private PluginExecutionContext pluginId(String pluginId) {
            this.pluginId = pluginId;
            return this;
        }

        private String pluginId() {
            return pluginId;
        }
    }

    private static final class PluginExecutionRecord extends ExecutionRecord {

        private final String pluginId;

        private PluginExecutionRecord(
                String executionId,
                String executorId,
                ExecutionStatus status,
                LocalDateTime startTime,
                LocalDateTime endTime,
                Map<String, Object> context,
                Map<String, Object> output,
                String message,
                String pluginId
        ) {
            super(executionId, executorId, status, startTime, endTime, context, output, message);
            this.pluginId = pluginId;
        }

        private String pluginId() {
            return pluginId;
        }
    }
}

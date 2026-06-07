package com.innospots.nexus.base.execution;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Immutable record of a completed execution. Captures the execution ID,
 * executor identity, status, timing, context snapshot, output, and
 * any status message.
 */
@Getter
public class ExecutionRecord {

    private final String executionId;
    private final String executorId;
    private final ExecutionStatus status;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Map<String, Object> context;
    private final Map<String, Object> output;
    private final String message;

    public ExecutionRecord(
            String executionId,
            String executorId,
            ExecutionStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Map<String, Object> context,
            Map<String, Object> output,
            String message
    ) {
        this.executionId = executionId;
        this.executorId = executorId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.context = context == null ? Map.of() : Map.copyOf(context);
        this.output = output == null ? Map.of() : Map.copyOf(output);
        this.message = message;
    }

    public String executionId() {
        return executionId;
    }

    public String executorId() {
        return executorId;
    }

    public ExecutionStatus status() {
        return status;
    }

    public LocalDateTime startTime() {
        return startTime;
    }

    public LocalDateTime endTime() {
        return endTime;
    }

    public Map<String, Object> context() {
        return context;
    }

    public Map<String, Object> output() {
        return output;
    }

    public String message() {
        return message;
    }
}

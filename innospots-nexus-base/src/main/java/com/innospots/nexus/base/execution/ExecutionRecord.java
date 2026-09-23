package com.innospots.nexus.base.execution;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 已完成执行的不变记录。捕获执行 ID、执行器标识、状态、时间、上下文快照、输出及状态消息。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ExecutionStatus
 * @see ExecutionContext
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

    /**
     * 创建执行记录。
     *
     * @param executionId 执行 ID
     * @param executorId  执行器 ID
     * @param status      执行状态
     * @param startTime   开始时间
     * @param endTime     结束时间
     * @param context     上下文快照
     * @param output      输出快照
     * @param message     状态消息
     */
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

    /**
     * 返回执行 ID。
     *
     * @return 执行 ID
     */
    public String executionId() {
        return executionId;
    }

    /**
     * 返回执行器 ID。
     *
     * @return 执行器 ID
     */
    public String executorId() {
        return executorId;
    }

    /**
     * 返回执行状态。
     *
     * @return 执行状态
     */
    public ExecutionStatus status() {
        return status;
    }

    /**
     * 返回开始时间。
     *
     * @return 开始时间
     */
    public LocalDateTime startTime() {
        return startTime;
    }

    /**
     * 返回结束时间。
     *
     * @return 结束时间
     */
    public LocalDateTime endTime() {
        return endTime;
    }

    /**
     * 返回上下文快照。
     *
     * @return 上下文映射
     */
    public Map<String, Object> context() {
        return context;
    }

    /**
     * 返回输出快照。
     *
     * @return 输出映射
     */
    public Map<String, Object> output() {
        return output;
    }

    /**
     * 返回状态消息。
     *
     * @return 状态消息
     */
    public String message() {
        return message;
    }
}

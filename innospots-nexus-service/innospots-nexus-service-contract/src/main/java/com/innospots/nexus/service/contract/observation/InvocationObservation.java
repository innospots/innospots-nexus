package com.innospots.nexus.service.contract.observation;

import java.time.Duration;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * 每次调用的指标记录器。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetricsProvider
 */
public interface InvocationObservation {

    /**
     * 记录首次输出耗时。
     *
     * @param latency 自开始起的延迟
     */
    void firstOutput(Duration latency);

    /**
     * 记录额外输出量。
     *
     * @param count 项数
     * @param bytes 字节数
     */
    void output(long count, long bytes);

    /**
     * 完成观测。
     *
     * @param outcome 终态结果
     */
    void finish(InvocationOutcome outcome);
}

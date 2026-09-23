package com.innospots.nexus.service.contract.trace;

import com.innospots.nexus.base.util.Checks;

/**
 * 不可变的 W3C 兼容追踪标识。禁用追踪时使用空字符串，而非伪造零值。
 *
 * @param traceId  追踪标识，关闭追踪时为空
 * @param spanId   Span 标识，关闭追踪时为空
 * @param sampled  是否采样
 * @author Smars
 * @date 2026/09/13
 * @see TraceProvider
 */
public record TraceSnapshot(String traceId, String spanId, boolean sampled) {

    public TraceSnapshot {
        Checks.notNull(traceId, "traceId");
        Checks.notNull(spanId, "spanId");
    }

    /**
     * 返回未采样且标识为空的快照。
     *
     * @return 空快照
     */
    public static TraceSnapshot empty() {
        return new TraceSnapshot("", "", false);
    }
}

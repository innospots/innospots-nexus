package com.innospots.nexus.base.util;

import java.util.Map;

/**
 * 指标计数器/计时器的时点快照，记录指标名称、标签、总次数及累计耗时（纳秒）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see MetricsUtils
 */
public record MetricsSnapshot(String name, Map<String, String> tags, long count, long totalNanos) {

    /**
     * 返回累计耗时的毫秒表示。
     *
     * @return 总耗时（毫秒）
     */
    public double totalMillis() {
        return totalNanos / 1_000_000D;
    }
}

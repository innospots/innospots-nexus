package com.innospots.nexus.service.contract.invocation;

import java.time.Duration;
import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * 拦截器、审计与指标使用的终态调用结果。
 *
 * @param type        结果分类
 * @param code        状态完整码
 * @param finishedAt  完成时刻
 * @param duration    耗时
 * @param outputCount 输出项数量
 * @param outputBytes 输出字节数
 * @author Smars
 * @date 2026/09/13
 * @see OutcomeType
 */
public record InvocationOutcome(
        OutcomeType type,
        String code,
        Instant finishedAt,
        Duration duration,
        long outputCount,
        long outputBytes
) {

    public InvocationOutcome {
        Checks.notNull(type, "type");
        Checks.notBlank(code, "code");
        Checks.notNull(finishedAt, "finishedAt");
        Checks.notNull(duration, "duration");
        Checks.isTrue(!duration.isNegative(), "duration must not be negative");
        Checks.isTrue(outputCount >= 0, "outputCount must not be negative");
        Checks.isTrue(outputBytes >= 0, "outputBytes must not be negative");
    }
}

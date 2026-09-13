package com.innospots.nexus.service.contract.invocation;

import java.time.Duration;
import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * Terminal invocation outcome used by interceptors, audit, and metrics.
 *
 * @param type        outcome classification
 * @param code        status full code
 * @param finishedAt  finish instant
 * @param duration    elapsed duration
 * @param outputCount emitted item count
 * @param outputBytes emitted byte count
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

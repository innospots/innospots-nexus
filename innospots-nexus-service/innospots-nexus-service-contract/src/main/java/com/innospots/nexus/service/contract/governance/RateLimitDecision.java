package com.innospots.nexus.service.contract.governance;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;

/**
 * Rate-limit decision. {@code retryAfter} is never negative.
 *
 * @param allowed    whether the request may proceed
 * @param retryAfter suggested wait, zero when allowed
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitProvider
 */
public record RateLimitDecision(boolean allowed, Duration retryAfter) {

    public RateLimitDecision {
        Checks.notNull(retryAfter, "retryAfter");
        Checks.isTrue(!retryAfter.isNegative(), "retryAfter must not be negative");
    }
}

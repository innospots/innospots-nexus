package com.innospots.nexus.service.contract.governance;

/**
 * Non-blocking rate limiter. Rejections do not wait.
 *
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitRequest
 * @see RateLimitDecision
 */
public interface RateLimitProvider {

    /**
     * Attempts to acquire tokens for {@code request}.
     *
     * @param request rate-limit request
     * @return decision
     */
    RateLimitDecision acquire(RateLimitRequest request);
}

package com.innospots.nexus.service.contract.governance;

import java.util.List;

import com.innospots.nexus.base.util.Checks;

/**
 * Rate-limit acquisition request. {@code cost} must be positive.
 *
 * @param policyKey  policy key
 * @param dimensions composite dimensions
 * @param cost       token cost
 * @author Smars
 * @date 2026/09/13
 * @see RateLimitProvider
 */
public record RateLimitRequest(String policyKey, List<String> dimensions, int cost) {

    public RateLimitRequest {
        Checks.notBlank(policyKey, "policyKey");
        dimensions = dimensions == null ? List.of() : List.copyOf(dimensions);
        Checks.positive(cost, "cost");
    }
}

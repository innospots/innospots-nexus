package com.innospots.nexus.service.governance.config;

/**
 * 单条限流策略如何组合 {@link com.innospots.nexus.service.governance.ratelimit.RateLimitDimensions#resolve} 维度。
 */
public enum RateLimitDimensionMode {

    /**
     * 默认：operation、principal、realm、tenant（若有）、customer（若有）等维度 AND 扣减。
     */
    COMPOSITE,

    /**
     * 仅按客户维度限流：有 {@code customerId} 时只扣 {@code customer:…} 桶；否则仅 {@code policyKey} 单桶。
     */
    CUSTOMER
}

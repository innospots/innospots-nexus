package com.innospots.nexus.service.governance.config;

import com.innospots.nexus.base.util.Checks;

/**
 * 单键<strong>舱壁并发</strong>参数。
 *
 * <p><strong>语义</strong>：同一 {@code policyKey} 最多允许 {@link #maxConcurrent()} 个调用同时持有许可。
 * 默认 {@link com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider} 使用
 * {@link java.util.concurrent.Semaphore#tryAcquire()}，<strong>无排队</strong>。</p>
 *
 * @param maxConcurrent 最大并发执行数，必须 &gt; 0
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.config.GovernanceConfig#bulkheads()
 */
public record BulkheadPolicy(
        /** 同一策略键下允许同时持有的最大并发许可数。 */
        int maxConcurrent) {

    /**
     * 校验并发度为正。
     */
    public BulkheadPolicy {
        Checks.isTrue(maxConcurrent > 0, "maxConcurrent must be positive");
    }
}

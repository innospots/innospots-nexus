package com.innospots.nexus.service.contract.governance;

import java.util.Optional;

/**
 * 舱壁（并发隔离）<strong>提供方 SPI</strong>。
 *
 * <p><strong>用途</strong>：限制同一 {@code policyKey} 下<strong>同时执行</strong>的调用数量，
 * 防止慢调用或资源耗尽拖垮整个进程。与 {@link RateLimitProvider} 互补：限流控制速率，舱壁控制并发。</p>
 *
 * <p><strong>使用场景</strong>：报表导出、批量任务、占用连接池的下游调用等，在 Application Service
 * 或 Client 边界通过 {@link com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected} 声明。</p>
 *
 * <p><strong>契约语义</strong>：</p>
 * <ul>
 *   <li>{@link #tryAcquire(String)} 必须<strong>非阻塞</strong>；无可用许可时返回 {@link Optional#empty()}，
 *       映射 {@link com.innospots.nexus.service.contract.status.ServiceStatusCode#CAPACITY_EXHAUSTED}（HTTP 503）。</li>
 *   <li>返回的 {@link BulkheadPermit} 必须在调用终态时 {@link BulkheadPermit#close()}，
 *       通常由 {@code BulkheadInterceptor} 的 {@code InvocationLease} 完成。</li>
 *   <li>配置中缺少 {@code policyKey} 时，默认实现视为配置错误而非静默放行。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see BulkheadPermit
 * @see com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider
 */
public interface BulkheadProvider {

    /**
     * 尝试获取一个并发许可。
     *
     * @param policyKey 舱壁策略键，对应 {@code GovernanceConfig.bulkheads}；不可为空白
     * @return 成功时包含须关闭的许可；舱壁已满时为空，调用方应拒绝本次调用
     */
    Optional<BulkheadPermit> tryAcquire(String policyKey);
}

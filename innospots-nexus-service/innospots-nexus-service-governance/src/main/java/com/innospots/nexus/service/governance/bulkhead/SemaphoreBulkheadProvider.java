package com.innospots.nexus.service.governance.bulkhead;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.governance.BulkheadPermit;
import com.innospots.nexus.service.contract.governance.BulkheadProvider;
import com.innospots.nexus.service.governance.config.BulkheadPolicy;
import com.innospots.nexus.service.governance.config.GovernanceConfig;

/**
 * 默认 {@link BulkheadProvider}：按策略键懒创建 {@link Semaphore}，{@link Semaphore#tryAcquire()} 无等待。
 *
 * <p><strong>用途</strong>：单 JVM 内限制同一 {@code policyKey} 的最大并发；与
 * {@link BulkheadInterceptor} 配合。配置来自 {@link GovernanceConfig#bulkheads()}。</p>
 *
 * <p><strong>约束</strong>：非公平、无排队；{@link BulkheadPermit#close()} 通过
 * {@link AtomicBoolean} 保证幂等 release。缺失策略键抛出 {@link NexusStatusCode#CONFIG_ERROR}。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see BulkheadPolicy
 * @see BulkheadInterceptor
 */
public final class SemaphoreBulkheadProvider implements BulkheadProvider {

    /** 舱壁策略键 → {@link BulkheadPolicy#maxConcurrent()} 配置源。 */
    private final GovernanceConfig config;

    /** 每个 {@code policyKey} 对应一个进程内信号量，懒创建且生命周期与 JVM 一致。 */
    private final ConcurrentHashMap<String, Semaphore> semaphores = new ConcurrentHashMap<>();

    /**
     * 创建提供方。
     *
     * @param config 含 {@code bulkheads} 表的治理配置
     */
    public SemaphoreBulkheadProvider(GovernanceConfig config) {
        this.config = Checks.notNull(config, "config");
    }

    /**
     * {@inheritDoc}
     *
     * @param policyKey 舱壁策略键，不可空白
     * @return 成功时含可关闭许可；并发已满时 {@link Optional#empty()}
     */
    @Override
    public Optional<BulkheadPermit> tryAcquire(String policyKey) {
        Checks.notBlank(policyKey, "policyKey");
        BulkheadPolicy policy = config.bulkheads().get(policyKey);
        if (policy == null) {
            throw NexusException.build(NexusStatusCode.CONFIG_ERROR);
        }
        Semaphore semaphore = semaphores.computeIfAbsent(policyKey, ignored -> new Semaphore(policy.maxConcurrent()));
        if (!semaphore.tryAcquire()) {
            return Optional.empty();
        }
        AtomicBoolean released = new AtomicBoolean(false);
        BulkheadPermit permit = () -> {
            if (released.compareAndSet(false, true)) {
                semaphore.release();
            }
        };
        return Optional.of(permit);
    }
}

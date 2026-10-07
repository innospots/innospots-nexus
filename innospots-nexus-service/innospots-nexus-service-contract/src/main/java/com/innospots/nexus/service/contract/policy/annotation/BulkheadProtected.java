package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明本操作需要应用<strong>命名舱壁（并发隔离）策略</strong>。
 *
 * <p><strong>用途</strong>：限制同一策略键下同时进行的调用数，保护线程池、连接池或 CPU，
 * 与 {@link RateLimited} 的速率控制互补。适用于重计算、导出、调用慢下游等场景。</p>
 *
 * <p><strong>配置</strong>：{@link #value()} 对应 {@code GovernanceConfig.bulkheads} 中的
 * {@code maxConcurrent} 等参数。默认实现无排队：许可不可用即 503。</p>
 *
 * <p><strong>约束</strong>：须进入 {@code InvocationEngine}；舱壁满时不应计入断路器失败统计。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.BulkheadProvider
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy#bulkheadKey()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface BulkheadProtected {

    /**
     * 舱壁策略键，映射 {@code GovernanceConfig.bulkheads}。
     *
     * @return 非空策略键名
     */
    String value();
}

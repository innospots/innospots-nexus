package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明本操作需要应用<strong>命名限流策略</strong>。
 *
 * <p><strong>用途</strong>：在 HTTP Controller、Application Service 或 Client 边界标记需限流的入口；
 * 运行时由 {@link com.innospots.nexus.service.contract.governance.RateLimitProvider} 在
 * {@code InvocationEngine} 链上判定是否放行。</p>
 *
 * <p><strong>配置</strong>：{@link #value()} 必须是 {@code GovernanceConfig.rateLimits} 中的键。
 * 数值 {@code burst}、{@code refillPerSecond} 在配置 Bean/YAML 中定义，而非注解参数。</p>
 *
 * <p><strong>使用约束</strong>：</p>
 * <ul>
 *   <li>须与引擎执行路径配合；Spring 默认通过 AOP 织入，亦可使用 {@code ServiceInvocationBridge}。</li>
 *   <li>可标注在类型或方法；方法级键覆盖类型级。</li>
 *   <li>Domain 层实体/仓储上禁止使用——限流属于传输或应用边界关注点。</li>
 *   <li>关闭治理而仍保留本注解时，按平台设计应启动失败（不能静默忽略）。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.RateLimitProvider
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy#rateLimitKey()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RateLimited {

    /**
     * 限流策略键，映射治理配置表 {@code rateLimits}。
     *
     * @return 非空策略键名（非 QPS 字面量）
     */
    String value();
}

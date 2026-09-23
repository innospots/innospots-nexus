package com.innospots.nexus.service.contract.policy.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 声明本操作受<strong>命名断路器</strong>保护（通常包裹下游 Client 调用）。
 *
 * <p><strong>用途</strong>：当某下游持续失败或慢调用比例过高时快速失败，避免级联阻塞。
 * 每个高风险下游应使用<strong>稳定</strong>的 {@link #value()}，不得按 requestId 动态建实例。</p>
 *
 * <p><strong>配置</strong>：{@link #value()} 映射 {@code GovernanceConfig.circuits} 中的
 * 滑动窗口、失败率、慢调用阈值等（见 {@code CircuitBreakerPolicy}）。</p>
 *
 * <p><strong>约束</strong>：</p>
 * <ul>
 *   <li>不默认用于所有入站 REST；仅对显式标注的下游边界生效。</li>
 *   <li>须在调用结束后上报 {@link com.innospots.nexus.service.contract.invocation.InvocationOutcome}。</li>
 *   <li>权限/限流/舱壁拒绝、客户端取消不应计入断路失败。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.CircuitBreakerProvider
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy#circuitKey()
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CircuitProtected {

    /**
     * 断路器策略键，映射 {@code GovernanceConfig.circuits}。
     *
     * @return 非空策略键名
     */
    String value();
}

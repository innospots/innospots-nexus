/**
 * 操作与<strong>治理声明注解</strong>：在应用边界标记策略意图，本身不包含数值限额或超时。
 *
 * <p>注解 {@code value()} 一律为<strong>配置键名</strong>（如 {@code "promo.claim"}），
 * 具体 burst、并发度、断路阈值、超时时长由装配层的 {@code GovernanceConfig} 提供。
 * 禁止在注解中写 {@code "10/min"} 或 {@code "5s"} 字面量。</p>
 *
 * <h2>生效前提</h2>
 * <p>声明注解<strong>不会</strong>单独触发治理；调用必须进入
 * {@link com.innospots.nexus.service.runtime.invocation.InvocationEngine} 拦截器链
 *（Spring：{@code GovernedInvocationAspect} 或 {@code ServiceInvocationBridge}；
 * Quarkus：显式 Bridge）。仅在 Controller 上贴注解而不经引擎执行时，策略不生效。</p>
 *
 * <h2>解析</h2>
 * <p>{@link com.innospots.nexus.service.runtime.policy.AnnotationPolicyResolver} 将类型/方法注解
 * 合并为 {@link com.innospots.nexus.service.contract.policy.OperationPolicy}。
 * 方法级注解优先于类型级（限流/舱壁/断路/超时键）。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.policy.annotation.ServiceOperation
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy
 */
package com.innospots.nexus.service.contract.policy.annotation;

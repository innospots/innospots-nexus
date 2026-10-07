/**
 * 治理<strong>数值配置</strong>与策略记录类型。
 *
 * <p>本包定义 {@link com.innospots.nexus.service.governance.config.GovernanceConfig} 及
 * 各子策略（限流、舱壁、断路器），供 {@code innospots-nexus-service-governance} 内 Provider
 * 与 Interceptor 读取。键名与 contract 注解 {@code value()} 一致。</p>
 *
 * <p><strong>装配</strong>：Spring/Quarkus adapter 通过 {@code @Bean} / {@code @Produces}
 * 提供 {@code GovernanceConfig} 实例；YAML 契约通常为 {@code service.governance.*}。
 * 本包类型保持 framework-neutral，不绑定配置属性类。</p>
 *
 * <p><strong>约束</strong>：构造时复制并校验 Map；缺失策略键在运行时由 Provider 抛出配置错误，
 * 不得静默降级为无限制。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.config.GovernanceConfig
 * @see com.innospots.nexus.service.contract.policy.annotation
 */
package com.innospots.nexus.service.governance.config;

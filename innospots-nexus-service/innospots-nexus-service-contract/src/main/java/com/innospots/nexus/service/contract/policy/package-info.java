/**
 * 操作<strong>策略模型</strong>：将注解或目录解析为
 * {@link com.innospots.nexus.service.contract.policy.OperationPolicy}，供
 * {@link com.innospots.nexus.service.contract.invocation.InvocationContext} 携带。
 *
 * <p>治理相关键字段（限流/舱壁/断路/超时）仅保存<strong>配置键名</strong>；
 * 数值在 governance 模块的 {@code GovernanceConfig} 中定义。
 * 注解声明见子包 {@link com.innospots.nexus.service.contract.policy.annotation}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy
 * @see com.innospots.nexus.service.contract.policy.PolicyCatalog
 */
package com.innospots.nexus.service.contract.policy;

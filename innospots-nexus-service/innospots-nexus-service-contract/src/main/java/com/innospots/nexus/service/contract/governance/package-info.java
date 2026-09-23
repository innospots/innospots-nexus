/**
 * 服务框架<strong>治理 SPI</strong>：限流、舱壁、断路器的可替换提供方契约。
 *
 * <p>本包只定义<strong>中立接口与值对象</strong>，不包含具体算法或中间件绑定。
 * 默认实现位于 {@code innospots-nexus-service-governance} 模块，由运行时
 * {@link com.innospots.nexus.service.contract.invocation.ServiceInterceptor} 在
 * {@code InvocationEngine} 链上调用。</p>
 *
 * <h2>使用方式（端到端）</h2>
 * <ol>
 *   <li>在应用边界方法上声明
 *       {@link com.innospots.nexus.service.contract.policy.annotation.RateLimited}、
 *       {@link com.innospots.nexus.service.contract.policy.annotation.BulkheadProtected}、
 *       {@link com.innospots.nexus.service.contract.policy.annotation.CircuitProtected}
 *       等注解（值为<strong>策略键名</strong>，不是数值）。</li>
 *   <li>在 {@code GovernanceConfig}（治理模块）中为各键配置 burst、并发度、断路参数等。</li>
 *   <li>通过 {@code InvocationEngine} 执行业务（Spring 可用注解 AOP 或
 *       {@code ServiceInvocationBridge}）。</li>
 * </ol>
 *
 * <h2>归属与约束</h2>
 * <ul>
 *   <li>属于 <strong>service-contract</strong>，不得依赖 Spring、Servlet 或具体治理实现。</li>
 *   <li>所有 {@code acquire}/{@code tryAcquire} 均为<strong>非阻塞</strong>：不得在 Provider 内等待配额恢复。</li>
 *   <li>许可（{@link BulkheadPermit}、{@link CircuitPermit}) 的生命周期由拦截器在调用终态时释放/上报。</li>
 *   <li>替换实现须保持线程安全；跨 JVM 一致限流由可选 {@code governance-redis} 模块或自定义
 *       {@link RateLimitProvider} 提供，契约本身不绑定存储。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.service.contract.governance.RateLimitProvider
 * @see com.innospots.nexus.service.contract.policy.OperationPolicy
 * @see com.innospots.nexus.service.contract.invocation.InterceptorIds
 */
package com.innospots.nexus.service.contract.governance;

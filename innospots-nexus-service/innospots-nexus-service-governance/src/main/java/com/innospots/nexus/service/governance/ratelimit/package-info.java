/**
 * <strong>限流</strong>默认实现：进程内复合维度令牌桶与 {@code InvocationEngine} 拦截器。
 *
 * <p>入口拦截器 id 为 {@link com.innospots.nexus.service.contract.invocation.InterceptorIds#RATE_LIMIT}，
 * order {@link com.innospots.nexus.service.contract.invocation.InterceptorOrders#RATE_LIMIT}（幂等之后、舱壁之前）。
 * 拒绝映射 429，且<strong>不</strong>占用舱壁许可。</p>
 *
 * <p>替换 {@link com.innospots.nexus.service.contract.governance.RateLimitProvider} 可接入 Redis 等集群限流，
 * 拦截器 {@link com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor} 无需修改。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.ratelimit.LocalTokenBucketProvider
 * @see com.innospots.nexus.service.governance.ratelimit.RateLimitInterceptor
 */
package com.innospots.nexus.service.governance.ratelimit;

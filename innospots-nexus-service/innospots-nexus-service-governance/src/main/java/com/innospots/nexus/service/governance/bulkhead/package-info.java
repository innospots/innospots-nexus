/**
 * <strong>舱壁</strong>默认实现：无排队信号量与拦截器。
 *
 * <p>拦截器 id {@link com.innospots.nexus.service.contract.invocation.InterceptorIds#BULKHEAD}，
 * order 在限流之后、断路之前。拒绝 HTTP 503，且不计入断路失败。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.bulkhead.SemaphoreBulkheadProvider
 * @see com.innospots.nexus.service.governance.bulkhead.BulkheadInterceptor
 */
package com.innospots.nexus.service.governance.bulkhead;

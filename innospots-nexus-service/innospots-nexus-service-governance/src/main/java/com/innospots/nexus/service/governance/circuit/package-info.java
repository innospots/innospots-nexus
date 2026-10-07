/**
 * <strong>断路器</strong>默认实现：Resilience4j 核心库与拦截器。
 *
 * <p>不引入 Spring Boot Resilience4j starter，仅依赖 {@code resilience4j-circuitbreaker}。
 * 拦截器 id {@link com.innospots.nexus.service.contract.invocation.InterceptorIds#CIRCUIT}，
 * 在舱壁之后进入；OPEN 时快速失败 {@link com.innospots.nexus.service.contract.status.ServiceStatusCode#CIRCUIT_OPEN}。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider
 * @see com.innospots.nexus.service.governance.circuit.CircuitBreakerInterceptor
 */
package com.innospots.nexus.service.governance.circuit;

package com.innospots.nexus.service.contract.governance;

/**
 * 断路器<strong>提供方 SPI</strong>。
 *
 * <p><strong>用途</strong>：对<strong>命名下游</strong>（稳定 {@code policyKey}）在失败率或慢调用过高时快速失败，
 * 避免持续压垮故障依赖。适用于 Client 边界、支付网关、外部 API 等，<strong>不</strong>建议默认保护所有入站 HTTP。</p>
 *
 * <p><strong>使用方式</strong>：</p>
 * <ol>
 *   <li>方法上 {@link com.innospots.nexus.service.contract.policy.annotation.CircuitProtected} 声明键名。</li>
 *   <li>{@code GovernanceConfig.circuits} 配置滑动窗口与阈值。</li>
 *   <li>{@link com.innospots.nexus.service.governance.circuit.CircuitBreakerInterceptor} 在 enter 时
 *       {@link #acquire(String)}，在调用终态时通过 {@link CircuitPermit#finish} 上报结果。</li>
 * </ol>
 *
 * <p><strong>约束</strong>：断路打开时 {@link #acquire(String)} 必须立即失败（默认
 * {@link com.innospots.nexus.service.contract.status.ServiceStatusCode#CIRCUIT_OPEN}，HTTP 503）；
 * 不得按 requestId 动态创建断路器实例。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see CircuitPermit
 * @see com.innospots.nexus.service.governance.circuit.Resilience4jCircuitBreakerProvider
 */
public interface CircuitBreakerProvider {

    /**
     * 在断路器允许的前提下获取一次调用许可。
     *
     * <p>调用方必须在获得 {@link CircuitPermit} 后执行下游逻辑，并在
     * {@link com.innospots.nexus.service.contract.invocation.InvocationOutcome} 确定后调用
     * {@link CircuitPermit#finish(com.innospots.nexus.service.contract.invocation.InvocationOutcome)}，
     * 以便统计成功/失败/慢调用。</p>
     *
     * @param policyKey 断路策略键，对应 {@code GovernanceConfig.circuits}；不可为空白
     * @return 非 {@code null} 的许可，须在终态时 {@code finish}
     * @throws com.innospots.nexus.base.exception.NexusException 断路为 OPEN 且不允许试探时（实现约定）
     */
    CircuitPermit acquire(String policyKey);
}

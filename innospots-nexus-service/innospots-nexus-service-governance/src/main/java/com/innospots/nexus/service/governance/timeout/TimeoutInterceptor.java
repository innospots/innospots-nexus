package com.innospots.nexus.service.governance.timeout;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.invocation.InvocationContext;
import com.innospots.nexus.service.contract.invocation.InvocationLease;
import com.innospots.nexus.service.contract.invocation.ServiceInterceptor;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;
import com.innospots.nexus.service.governance.config.GovernanceConfig;

/**
 * 为单次调用安装<strong>操作级超时</strong>并与全局 deadline 取最小值。
 *
 * <p><strong>解析顺序</strong>（{@link #resolveTimeout}）：</p>
 * <ol>
 *   <li>{@link com.innospots.nexus.service.contract.policy.OperationPolicy#timeout()} 固定时长</li>
 *   <li>{@link com.innospots.nexus.service.contract.policy.OperationPolicy#timeoutPolicyKey()}
 *       → {@link GovernanceConfig#timeouts()}</li>
 *   <li>{@link InvocationContext#operationId()} → 同表</li>
 * </ol>
 *
 * <p><strong>行为</strong>：无解析结果则透传；有效时长为零或负则
 * {@link ServiceStatusCode#DEADLINE_EXCEEDED}；否则 {@link OperationTimeoutArmer#arm}，
 * lease finish 时关闭 registration。业务应协作检查 cancellation。</p>
 *
 * <p><strong>注意</strong>：order=15，位于认证之前安装定时器；极小超时时认证链路过长可能先触发取消。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see OperationTimeoutArmer
 * @see GovernanceConfig#timeouts()
 */
public final class TimeoutInterceptor implements ServiceInterceptor {

    /** 拦截器顺序：紧接 {@code runtime.deadline}(10)，早于认证(20)。 */
    private static final int ORDER = 15;

    /** 拦截器唯一标识，与运行时注册表一致。 */
    private static final String ID = "governance.timeout";

    /** 命名超时表 {@link GovernanceConfig#timeouts()} 及传输默认时长配置。 */
    private final GovernanceConfig config;

    /** 将有效超时时长绑定到请求级取消源（由 Spring/Quarkus adapter 实现）。 */
    private final OperationTimeoutArmer armer;

    /**
     * 创建拦截器。
     *
     * @param config 含 {@code timeouts} 表的治理配置
     * @param armer  由 adapter 提供的取消安装实现
     */
    public TimeoutInterceptor(GovernanceConfig config, OperationTimeoutArmer armer) {
        this.config = Checks.notNull(config, "config");
        this.armer = Checks.notNull(armer, "armer");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code governance.timeout}
     */
    @Override
    public String id() {
        return ID;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code 15}（紧接 deadline 拦截器之后）
     */
    @Override
    public int order() {
        return ORDER;
    }

    /**
     * 解析超时并安装取消，或透传/拒绝。
     *
     * @param invocation 当前调用
     * @return lease 在 finish 时关闭 {@link CancellationRegistration}
     */
    @Override
    public CompletionStage<InvocationLease> enter(InvocationContext invocation) {
        Duration timeout = resolveTimeout(invocation);
        if (timeout == null) {
            return CompletableFuture.completedFuture(outcome -> CompletableFuture.completedFuture(null));
        }
        Duration effective = min(timeout, invocation.service().deadline().remaining());
        if (effective.isZero() || effective.isNegative()) {
            return CompletableFuture.failedFuture(NexusException.build(ServiceStatusCode.DEADLINE_EXCEEDED));
        }
        CancellationRegistration registration = armer.arm(invocation, effective);
        return CompletableFuture.completedFuture(outcome -> {
            registration.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * 按策略与配置表解析操作超时时长。
     *
     * @param invocation 调用上下文
     * @return 时长或 {@code null} 表示不安装操作超时
     */
    private Duration resolveTimeout(InvocationContext invocation) {
        if (invocation.policy().timeout() != null) {
            return invocation.policy().timeout();
        }
        String timeoutKey = invocation.policy().timeoutPolicyKey();
        if (timeoutKey != null) {
            Duration configured = config.timeouts().get(timeoutKey);
            if (configured != null) {
                return configured;
            }
        }
        return config.timeouts().get(invocation.operationId());
    }

    private static Duration min(Duration left, Duration right) {
        if (left.compareTo(right) <= 0) {
            return left;
        }
        return right;
    }
}

package com.innospots.nexus.service.governance.timeout;

import java.time.Duration;

import com.innospots.nexus.service.contract.cancellation.CancellationRegistration;
import com.innospots.nexus.service.contract.invocation.InvocationContext;

/**
 * 将操作级超时绑定到运行时<strong>取消源</strong>的 adapter SPI。
 *
 * <p><strong>用途</strong>：{@link TimeoutInterceptor} 解析出有效 {@link Duration} 后调用本接口；
 * 实现方（如 Spring {@code DeadlineOperationTimeoutArmer}）在超时到达时向当前请求的
 * {@link com.innospots.nexus.service.contract.cancellation.CancellationSource} 发取消信号。
 * 返回的 {@link CancellationRegistration} 须在调用 finish 时 {@link CancellationRegistration#close()}
 * 撤销定时任务。</p>
 *
 * <p><strong>约束</strong>：不得在 {@link #arm} 内阻塞；须与 {@code DeadlineScheduler} 和
 * 请求生命周期 accessor 配合，保证仅在有效 {@link InvocationContext} 下安装。</p>
 *
 * @author Smars
 * @date 2026/09/15
 * @see TimeoutInterceptor
 * @see com.innospots.nexus.service.contract.cancellation.CancellationRegistration
 */
@FunctionalInterface
public interface OperationTimeoutArmer {

    /**
     * 在 {@code timeout} 到期时请求取消当前操作。
     *
     * @param invocation 当前调用，含 ServiceContext 与 deadline
     * @param timeout    已与全局 deadline 取 min 后的有效正数时长
     * @return 可关闭的注册句柄，用于在调用结束时取消定时器
     */
    CancellationRegistration arm(InvocationContext invocation, Duration timeout);
}

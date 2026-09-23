package com.innospots.nexus.service.contract.governance;

import com.innospots.nexus.service.contract.invocation.InvocationOutcome;

/**
 * 断路器调用许可：负责把<strong>单次调用的终态</strong>反馈给断路器统计。
 *
 * <p><strong>用途</strong>：与舱壁不同，断路器需要在知道调用结果后才更新滑动窗口。
 * {@link CircuitBreakerProvider#acquire(String)} 在 enter 阶段同步获取许可；
 * {@link #finish(InvocationOutcome)} 在 {@code InvocationEngine} 逆序释放 lease 时调用。</p>
 *
 * <p><strong>实现约束</strong>：应对权限拒绝、限流、舱壁拒绝、客户端取消等 outcome <strong>不计入</strong>
 * 下游失败（见默认实现的 {@code shouldRecordFailure}）；仅真实执行后的 FAILED/TIMED_OUT 等计入。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see CircuitBreakerProvider
 * @see InvocationOutcome
 */
@FunctionalInterface
public interface CircuitPermit {

    /**
     * 向断路器报告本次调用的逻辑终态。
     *
     * @param outcome 引擎汇总的终态，含类型、状态码与耗时；不可为 {@code null}
     */
    void finish(InvocationOutcome outcome);
}

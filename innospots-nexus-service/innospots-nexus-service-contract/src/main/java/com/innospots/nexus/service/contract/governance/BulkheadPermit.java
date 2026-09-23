package com.innospots.nexus.service.contract.governance;

/**
 * 舱壁并发许可句柄。
 *
 * <p><strong>用途</strong>：表示占用了一个舱壁槽位，在业务调用结束（成功、失败或取消）后必须释放，
 * 以便其他调用可获得并发配额。由 {@link BulkheadProvider#tryAcquire(String)} 创建，
 * 由拦截器在 {@link com.innospots.nexus.service.contract.invocation.InvocationLease#finish} 阶段关闭。</p>
 *
 * <p><strong>约束</strong>：</p>
 * <ul>
 *   <li>{@link #close()} 必须幂等：重复关闭不得 double-release 信号量。</li>
 *   <li>不得在 {@code close()} 中执行可能阻塞的业务逻辑。</li>
 *   <li>若业务线程孤儿化未终止，许可可能延迟释放（见 runtime 设计中的 workTermination 模型）。</li>
 * </ul>
 *
 * @author Smars
 * @date 2026/09/13
 * @see BulkheadProvider
 */
@FunctionalInterface
public interface BulkheadPermit extends AutoCloseable {

    /**
     * 释放舱壁许可。
     *
     * <p>实现应保证多次调用安全；拦截器与引擎保证每个 permit 至多 finish 一次。</p>
     */
    @Override
    void close();
}

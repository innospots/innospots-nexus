package com.innospots.nexus.core.plugin.contribution;

/**
 * 一个已准备但尚未对外可见的 Contribution 事务句柄。
 *
 * <p>实现必须支持幂等的 {@link #stage()}、{@link #commit()}、{@link #rollback()}
 * 和 {@link #close()}；调用方按 stage → commit → close 顺序驱动，失败时回滚。</p>
 */
public interface PreparedPluginContribution extends AutoCloseable {

    /** 预提交资源，必须幂等。 */
    void stage();

    /** 提交资源索引，必须幂等且不执行高风险 I/O。 */
    void commit();

    /** 回滚已经 stage 或 commit 的资源，必须幂等。 */
    void rollback();

    /** 释放句柄和临时资源，必须幂等。 */
    @Override
    void close();
}

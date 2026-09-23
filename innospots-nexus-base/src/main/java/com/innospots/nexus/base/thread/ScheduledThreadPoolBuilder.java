package com.innospots.nexus.base.thread;

import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 命名 {@link ScheduledThreadPoolExecutor} 的流式构建器；默认定时类后台线程（守护线程）。
 *
 * @author Smars
 * @date 2026/09/19
 * @see ThreadExecutionRole
 * @see NexusThreadFactory
 */
public final class ScheduledThreadPoolBuilder {

    public static final int DEFAULT_POOL_SIZE = 1;

    private final String poolName;
    private int poolSize = DEFAULT_POOL_SIZE;
    private ThreadExecutionRole role = ThreadExecutionRole.BACKGROUND;
    private boolean removeOnCancelPolicy = true;

    private ScheduledThreadPoolBuilder(String poolName) {
        this.poolName = poolName;
    }

    /**
     * 创建指定池名称的构建器。
     *
     * @param poolName 线程池名称前缀
     * @return 构建器实例
     */
    public static ScheduledThreadPoolBuilder builder(String poolName) {
        return new ScheduledThreadPoolBuilder(poolName);
    }

    /**
     * 设置调度线程数（最小为 1）。
     *
     * @param poolSize 线程数
     * @return 当前构建器
     */
    public ScheduledThreadPoolBuilder poolSize(int poolSize) {
        this.poolSize = Math.max(1, poolSize);
        return this;
    }

    /**
     * 使用守护后台线程（默认定时/超时场景）。
     *
     * @return 当前构建器
     */
    public ScheduledThreadPoolBuilder background() {
        this.role = ThreadExecutionRole.BACKGROUND;
        return this;
    }

    /**
     * 使用非守护业务工作线程。
     *
     * @return 当前构建器
     */
    public ScheduledThreadPoolBuilder worker() {
        this.role = ThreadExecutionRole.WORKER;
        return this;
    }

    /**
     * 设置执行角色。
     *
     * @param role 角色
     * @return 当前构建器
     */
    public ScheduledThreadPoolBuilder role(ThreadExecutionRole role) {
        if (role != null) {
            this.role = role;
        }
        return this;
    }

    /**
     * 是否对已取消的延迟任务从队列移除（默认 {@code true}）。
     *
     * @param removeOnCancelPolicy 策略开关
     * @return 当前构建器
     */
    public ScheduledThreadPoolBuilder removeOnCancelPolicy(boolean removeOnCancelPolicy) {
        this.removeOnCancelPolicy = removeOnCancelPolicy;
        return this;
    }

    /**
     * 构建 {@link ScheduledThreadPoolExecutor}。
     *
     * @return 调度线程池
     */
    public ScheduledThreadPoolExecutor build() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                poolSize,
                new NexusThreadFactory(poolName, role));
        executor.setRemoveOnCancelPolicy(removeOnCancelPolicy);
        return executor;
    }
}

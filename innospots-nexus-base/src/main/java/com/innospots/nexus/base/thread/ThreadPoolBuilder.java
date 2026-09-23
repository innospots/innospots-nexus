package com.innospots.nexus.base.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link NexusThreadPoolExecutor} 的流式构建器。默认值：核心线程数 = 可用处理器数，
 * 最大线程数 = 核心线程数，队列容量 = 20k，空闲存活时间 = 120s，
 * 默认 {@link ThreadExecutionRole#WORKER}，拒绝策略 = 调用者运行策略。
 *
 * @author Smars
 * @date 2026/09/13
 * @see NexusThreadPoolExecutor
 * @see NexusThreadFactory
 */
public final class ThreadPoolBuilder {

    public static final int DEFAULT_QUEUE_CAPACITY = 20_000;
    public static final int DEFAULT_KEEP_ALIVE_SECONDS = 120;

    private final String poolName;
    private int coreSize = Runtime.getRuntime().availableProcessors();
    private int maxSize = coreSize;
    private int queueCapacity = DEFAULT_QUEUE_CAPACITY;
    private int keepAliveSeconds = DEFAULT_KEEP_ALIVE_SECONDS;
    private ThreadExecutionRole role = ThreadExecutionRole.WORKER;
    private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

    private ThreadPoolBuilder(String poolName) {
        this.poolName = poolName;
    }

    /**
     * 创建指定池名称的构建器。
     *
     * @param poolName 线程池名称
     * @return 构建器实例
     */
    public static ThreadPoolBuilder builder(String poolName) {
        return new ThreadPoolBuilder(poolName);
    }

    /**
     * 一步构建线程池的便捷方法。
     *
     * @param coreSize      核心线程数
     * @param maxSize       最大线程数
     * @param queueCapacity 队列容量
     * @param poolName      线程池名称
     * @return 构建完成的线程池
     */
    public static NexusThreadPoolExecutor build(int coreSize, int maxSize, int queueCapacity, String poolName) {
        return builder(poolName).coreSize(coreSize).maxSize(maxSize).queueCapacity(queueCapacity).build();
    }

    /**
     * 设置核心线程数（最小为 1）。
     *
     * @param coreSize 核心线程数
     * @return 当前构建器
     */
    public ThreadPoolBuilder coreSize(int coreSize) {
        this.coreSize = Math.max(1, coreSize);
        return this;
    }

    /**
     * 设置最大线程数（最小为 1）。
     *
     * @param maxSize 最大线程数
     * @return 当前构建器
     */
    public ThreadPoolBuilder maxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        return this;
    }

    /**
     * 设置工作队列容量。零或负数时使用 {@link SynchronousQueue}。
     *
     * @param queueCapacity 队列容量
     * @return 当前构建器
     */
    public ThreadPoolBuilder queueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
        return this;
    }

    /**
     * 设置空闲线程的存活时间（秒）。
     *
     * @param keepAliveSeconds 存活时间（秒）
     * @return 当前构建器
     */
    public ThreadPoolBuilder keepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = Math.max(0, keepAliveSeconds);
        return this;
    }

    /**
     * 使用守护后台线程（定时、清理等不阻塞 JVM 退出的任务）。
     *
     * @return 当前构建器
     */
    public ThreadPoolBuilder background() {
        return role(ThreadExecutionRole.BACKGROUND);
    }

    /**
     * 使用非守护业务工作线程（默认）。
     *
     * @return 当前构建器
     */
    public ThreadPoolBuilder worker() {
        return role(ThreadExecutionRole.WORKER);
    }

    /**
     * 设置线程执行角色。
     *
     * @param role 角色
     * @return 当前构建器
     */
    public ThreadPoolBuilder role(ThreadExecutionRole role) {
        if (role != null) {
            this.role = role;
        }
        return this;
    }

    /**
     * 设置工作线程是否为守护线程。
     *
     * @param daemon 是否为守护线程
     * @return 当前构建器
     */
    public ThreadPoolBuilder daemon(boolean daemon) {
        return role(daemon ? ThreadExecutionRole.BACKGROUND : ThreadExecutionRole.WORKER);
    }

    /**
     * 设置任务拒绝处理策略（默认：{@link ThreadPoolExecutor.CallerRunsPolicy}）。
     *
     * @param rejectedExecutionHandler 拒绝处理策略
     * @return 当前构建器
     */
    public ThreadPoolBuilder rejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
        if (rejectedExecutionHandler != null) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
        }
        return this;
    }

    /**
     * 构建 {@link NexusThreadPoolExecutor}。最大线程数会被规范化为至少等于核心线程数。
     *
     * @return 构建完成的线程池
     */
    public NexusThreadPoolExecutor build() {
        int normalizedMax = Math.max(coreSize, maxSize);
        return new NexusThreadPoolExecutor(
                poolName,
                coreSize,
                normalizedMax,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                createQueue(queueCapacity),
                new NexusThreadFactory(poolName, role),
                rejectedExecutionHandler
        );
    }

    /**
     * 容量大于 0 时创建有界队列，否则创建 {@link SynchronousQueue}。
     *
     * @param queueCapacity 队列容量
     * @return 工作队列
     */
    static BlockingQueue<Runnable> createQueue(int queueCapacity) {
        return queueCapacity > 0 ? new ArrayBlockingQueue<>(queueCapacity) : new SynchronousQueue<>();
    }
}

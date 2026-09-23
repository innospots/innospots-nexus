package com.innospots.nexus.base.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 命名 {@link ThreadFactory}，按 {@link ThreadExecutionRole} 配置守护线程与名称前缀。
 *
 * @author Smars
 * @date 2026/09/13
 * @see ThreadFactory
 * @see ThreadExecutionRole
 */
public final class NexusThreadFactory implements ThreadFactory {

    private final String namePrefix;
    private final ThreadExecutionRole role;
    private final AtomicInteger sequence = new AtomicInteger(1);

    /**
     * 创建非守护（{@link ThreadExecutionRole#WORKER}）线程工厂。
     *
     * @param namePrefix 线程名称前缀
     */
    public NexusThreadFactory(String namePrefix) {
        this(namePrefix, ThreadExecutionRole.WORKER);
    }

    /**
     * 按布尔守护标志创建工厂（等价于 {@link ThreadExecutionRole}）。
     *
     * @param namePrefix 线程名称前缀；为 null 或空白时默认 {@code nexus-worker}
     * @param daemon     是否为守护线程
     */
    public NexusThreadFactory(String namePrefix, boolean daemon) {
        this(namePrefix, daemon ? ThreadExecutionRole.BACKGROUND : ThreadExecutionRole.WORKER);
    }

    /**
     * 创建线程工厂。
     *
     * @param namePrefix 线程名称前缀；为 null 或空白时默认 {@code nexus-worker}
     * @param role       执行角色
     */
    public NexusThreadFactory(String namePrefix, ThreadExecutionRole role) {
        this.namePrefix = namePrefix == null || namePrefix.isBlank() ? "nexus-worker" : namePrefix;
        this.role = role == null ? ThreadExecutionRole.WORKER : role;
    }

    /**
     * 业务工作线程工厂（非守护）。
     *
     * @param namePrefix 名称前缀
     * @return 工厂实例
     */
    public static NexusThreadFactory worker(String namePrefix) {
        return new NexusThreadFactory(namePrefix, ThreadExecutionRole.WORKER);
    }

    /**
     * 后台执行线程工厂（守护）。
     *
     * @param namePrefix 名称前缀
     * @return 工厂实例
     */
    public static NexusThreadFactory background(String namePrefix) {
        return new NexusThreadFactory(namePrefix, ThreadExecutionRole.BACKGROUND);
    }

    /**
     * 返回配置的执行角色。
     *
     * @return 角色
     */
    public ThreadExecutionRole executionRole() {
        return role;
    }

    /**
     * 创建新线程。
     *
     * @param runnable 线程任务
     * @return 新创建的线程
     */
    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, namePrefix + "-" + sequence.getAndIncrement());
        thread.setDaemon(role.daemon());
        return thread;
    }
}

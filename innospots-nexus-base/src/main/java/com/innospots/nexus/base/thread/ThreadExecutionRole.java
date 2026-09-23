package com.innospots.nexus.base.thread;

/**
 * 线程执行角色：区分需随进程优雅收尾的业务工作线程与不阻塞 JVM 退出的后台线程。
 *
 * @author Smars
 * @date 2026/09/19
 * @see NexusThreadFactory
 * @see ThreadPoolBuilder
 * @see ScheduledThreadPoolBuilder
 */
public enum ThreadExecutionRole {

    /** 非守护线程，适合业务异步与需完整生命周期的任务。 */
    WORKER(false),

    /** 守护线程，适合定时、超时、清理等后台任务。 */
    BACKGROUND(true);

    private final boolean daemon;

    ThreadExecutionRole(boolean daemon) {
        this.daemon = daemon;
    }

    /**
     * 是否以守护线程运行。
     *
     * @return 后台角色为 {@code true}
     */
    public boolean daemon() {
        return daemon;
    }
}

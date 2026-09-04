package com.innospots.nexus.core.bootstrap;

/**
 * 框架无关的启动后初始化步骤。
 */
public interface NexusStartupTask {

    /** 任务名称，用于日志与排错。 */
    String name();

    /** 执行顺序；数值越小越先执行。 */
    int order();

    /**
     * 执行启动步骤。
     *
     * @param context 跨步骤共享的轻量上下文
     */
    void run(NexusStartupContext context);
}

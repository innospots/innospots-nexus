package com.innospots.nexus.core.watcher.contract;

import com.innospots.nexus.core.watcher.runtime.AbstractWatcher;
import com.innospots.nexus.core.watcher.runtime.WatcherSupervisor;

/**
 * 后台 Watcher 接口，继承 {@link Runnable} 以便在线程池中执行。
 * 生命周期为循环执行 {@link #check()} → {@link #execute()}，直至被停止。
 *
 * @author Smars
 * @date 2026/09/13
 * @see AbstractWatcher
 * @see WatcherSupervisor
 */
public interface IWatcher extends Runnable {

    /** 供日志使用的可读 Watcher 名称。 */
    String name();

    /** Watcher 是否正在执行其循环。 */
    boolean isRunning();

    /**
     * 执行前检查；返回 {@code false} 时跳过本轮。
     * 默认实现始终返回 {@code true}。
     *
     * @return 是否继续执行本轮
     */
    default boolean check() {
        return true;
    }

    /**
     * 执行一轮工作，并返回下一轮之前的间隔（毫秒）。
     * 返回值 &le; 0 时回退到默认间隔。
     *
     * @return 下一轮间隔（毫秒）
     */
    int execute();

    /** 请求在当前轮结束后停止 Watcher。 */
    void stop();
}

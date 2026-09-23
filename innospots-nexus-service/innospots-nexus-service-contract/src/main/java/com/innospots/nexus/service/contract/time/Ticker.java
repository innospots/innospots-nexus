package com.innospots.nexus.service.contract.time;

/**
 * {@link Deadline} 使用的单调纳秒时钟源。
 *
 * @author Smars
 * @date 2026/09/13
 * @see Deadline
 */
@FunctionalInterface
public interface Ticker {

    /**
     * 返回单调纳秒时间戳。
     *
     * @return 纳秒数
     */
    long readNanos();

    /**
     * 返回基于 {@link System#nanoTime()} 的 ticker。
     *
     * @return 系统 ticker
     */
    static Ticker system() {
        return System::nanoTime;
    }
}

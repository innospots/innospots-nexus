package com.innospots.nexus.core.plugin.event;

/**
 * 可幂等取消的事件订阅句柄。
 * @author Smars
 * @date 2026/09/13
 */
public interface Subscription extends AutoCloseable {

    /** 最多移除该订阅一次。 */
    @Override
    void close();
}

package com.innospots.nexus.service.stream.session;

import java.time.Instant;
import java.util.concurrent.Flow;

import com.innospots.nexus.service.stream.channel.StreamChannel;
import com.innospots.nexus.service.stream.event.StreamEvent;

/**
 * 完整流会话，暴露 publisher 与通道。
 *
 * @param <T> 事件载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public interface StreamSession<T> extends StreamSink<T> {

    /**
     * 返回创建时间。
     *
     * @return 创建时间
     */
    Instant createdAt();

    /**
     * 返回最近业务活动时间。
     *
     * @return 活动时间
     */
    Instant lastActivityAt();

    /**
     * 返回底层事件通道。
     *
     * @return 通道
     */
    StreamChannel<StreamEvent<T>> channel();

    /**
     * 返回事件发布者。
     *
     * @return 发布者
     */
    Flow.Publisher<StreamEvent<T>> publisher();
}

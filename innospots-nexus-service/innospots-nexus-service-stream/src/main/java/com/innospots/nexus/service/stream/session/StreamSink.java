package com.innospots.nexus.service.stream.session;

import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.stream.channel.EmitResult;
import com.innospots.nexus.service.stream.channel.StreamChannel;
import com.innospots.nexus.service.stream.event.StreamEvent;

/**
 * 业务侧流 emit 入口。
 *
 * @param <T> 事件载荷类型
 * @author Smars
 * @date 2026/09/15
 */
public interface StreamSink<T> {

    /**
     * 返回会话标识。
     *
     * @return 会话标识
     */
    String sessionId();

    /**
     * 返回当前状态。
     *
     * @return 状态
     */
    StreamState state();

    /**
     * 返回是否已取消。
     *
     * @return 已取消时为 {@code true}
     */
    boolean isCancelled();

    /**
     * 以默认类型 {@code message} emit。
     *
     * @param data 载荷
     * @return emit 结果
     */
    CompletionStage<EmitResult> emit(T data);

    /**
     * 以指定类型 emit。
     *
     * @param type 事件类型
     * @param data 载荷
     * @return emit 结果
     */
    CompletionStage<EmitResult> emit(String type, T data);

    /**
     * 正常完成会话。
     *
     * @return 完成阶段
     */
    CompletionStage<Void> complete();

    /**
     * 以失败终止会话。
     *
     * @param failure 失败
     * @return 完成阶段
     */
    CompletionStage<Void> fail(NexusException failure);

    /**
     * 取消会话。
     *
     * @param reason 取消原因
     * @return 完成阶段
     */
    CompletionStage<Void> cancel(CancellationReason reason);
}

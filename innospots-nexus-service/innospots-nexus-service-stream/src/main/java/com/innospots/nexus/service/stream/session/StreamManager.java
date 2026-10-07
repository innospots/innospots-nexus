package com.innospots.nexus.service.stream.session;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.stream.channel.EmitResult;

/**
 * 流会话注册表。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface StreamManager {

    /**
     * 打开新会话。
     *
     * @param eventType 事件类型
     * @param <T>       载荷类型
     * @return sink
     */
    <T> StreamSink<T> open(Class<T> eventType);

    /**
     * 打开新会话。
     *
     * @param eventType 事件类型
     * @param <T>       载荷类型
     * @return sink
     */
    <T> StreamSink<T> open(Type eventType);

    /**
     * 查找会话快照。
     *
     * @param sessionId 会话标识
     * @return 快照
     */
    Optional<StreamSnapshot> find(String sessionId);

    /**
     * 向指定会话 emit。
     *
     * @param sessionId 会话标识
     * @param type      事件类型
     * @param data      载荷
     * @param <T>       载荷类型
     * @return emit 结果
     */
    <T> CompletionStage<EmitResult> emit(String sessionId, String type, T data);

    /**
     * 使会话失败。
     *
     * @param sessionId 会话标识
     * @param failure   失败
     * @return 完成阶段
     */
    CompletionStage<Void> fail(String sessionId, NexusException failure);

    /**
     * 取消会话。
     *
     * @param sessionId 会话标识
     * @param reason    取消原因
     * @return 完成阶段
     */
    CompletionStage<Void> cancel(String sessionId, CancellationReason reason);

    /**
     * 关闭管理器并排空所有会话。
     *
     * @return 完成阶段
     */
    CompletionStage<Void> close();
}

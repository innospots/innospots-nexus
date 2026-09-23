package com.innospots.nexus.service.stream.channel;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.ToLongFunction;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.service.contract.cancellation.CancellationReason;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;

/**
 * 有界单订阅流通道。
 *
 * @param <T> 元素类型
 * @author Smars
 * @date 2026/09/15
 */
public interface StreamChannel<T> {

    /**
     * 向有界队列 emit 元素。
     *
     * @param value 元素
     * @return emit 结果
     */
    CompletionStage<EmitResult> emit(T value);

    /**
     * 返回单订阅发布者。
     *
     * @return 发布者
     */
    Flow.Publisher<T> publisher();

    /**
     * 正常完成通道。
     *
     * @return 完成阶段
     */
    CompletionStage<Void> complete();

    /**
     * 以失败终止通道。
     *
     * @param failure 失败
     * @return 完成阶段
     */
    CompletionStage<Void> fail(NexusException failure);

    /**
     * 以取消终止通道。
     *
     * @param reason 取消原因
     * @return 完成阶段
     */
    CompletionStage<Void> cancel(CancellationReason reason);
}

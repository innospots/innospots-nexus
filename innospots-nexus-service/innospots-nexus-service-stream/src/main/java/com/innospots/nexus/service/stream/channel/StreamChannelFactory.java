package com.innospots.nexus.service.stream.channel;

import java.util.function.ToLongFunction;

import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.stream.config.StreamConfig;

/**
 * 创建 {@link StreamChannel} 实例。
 *
 * @author Smars
 * @date 2026/09/15
 */
public interface StreamChannelFactory {

    /**
     * 创建有界通道。
     *
     * @param config         配置
     * @param token          取消令牌
     * @param sizeEstimator  元素大小估算
     * @param <T>            元素类型
     * @return 通道
     */
    <T> StreamChannel<T> create(
            StreamConfig config,
            CancellationToken token,
            ToLongFunction<T> sizeEstimator);
}

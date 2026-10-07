package com.innospots.nexus.service.stream.channel;

import java.util.function.ToLongFunction;

import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.stream.config.StreamConfig;

/**
 * 默认 {@link StreamChannelFactory}，创建 {@link BoundedStreamChannel}。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class DefaultStreamChannelFactory implements StreamChannelFactory {

    @Override
    public <T> StreamChannel<T> create(
            StreamConfig config,
            CancellationToken token,
            ToLongFunction<T> sizeEstimator) {
        return new BoundedStreamChannel<>(config, token, sizeEstimator);
    }
}

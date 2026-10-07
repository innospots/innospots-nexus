package com.innospots.nexus.service.adapter.test.fixture;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.service.contract.invocation.ExecutionMode;
import com.innospots.nexus.service.websocket.message.FrameType;
import com.innospots.nexus.service.websocket.message.JsonWebSocketCodec;
import com.innospots.nexus.service.websocket.message.MessageDescriptor;

/**
 * adapter WebSocket 夹具工厂。
 */
public final class AdapterWebSocketFixtures {

    public static final String READY_TYPE = "adapter.ready";

    /** 带 {@code adapter-rate-limit} 治理键的探测消息类型。 */
    public static final String RATE_LIMIT_PROBE_TYPE = "adapter.rate-probe";

    /** adapter 宿主默认 {@code GovernanceConfig} 中的限流策略键。 */
    public static final String ADAPTER_WS_RATE_LIMIT_KEY = "adapter-ws-rate-limit";

    private AdapterWebSocketFixtures() {
    }

    /**
     * 返回 adapter 场景使用的 JSON codec。
     *
     * @return codec
     */
    public static JsonWebSocketCodec codec() {
        return JsonWebSocketCodec.builder()
                .maxMessageBytes(64 * 1024)
                .descriptors(Map.of(
                        READY_TYPE,
                        new MessageDescriptor(
                                READY_TYPE,
                                String.class,
                                String.class,
                                Set.of(),
                                null,
                                ExecutionMode.BLOCKING,
                                FrameType.TEXT,
                                null,
                                null,
                                null,
                                null),
                        RATE_LIMIT_PROBE_TYPE,
                        new MessageDescriptor(
                                RATE_LIMIT_PROBE_TYPE,
                                String.class,
                                String.class,
                                Set.of(),
                                null,
                                ExecutionMode.BLOCKING,
                                FrameType.TEXT,
                                ADAPTER_WS_RATE_LIMIT_KEY,
                                null,
                                null,
                                null)))
                .build();
    }
}

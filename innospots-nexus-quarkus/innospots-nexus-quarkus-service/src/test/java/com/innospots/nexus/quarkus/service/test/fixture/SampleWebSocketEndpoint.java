package com.innospots.nexus.quarkus.service.test.fixture;

import com.innospots.nexus.quarkus.service.websocket.QuarkusWebSocketEndpointBridge;
import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;

/**
 * adapter-test WebSocket 夹具端点。
 */
@WebSocket(path = AdapterScenarioPaths.WEBSOCKET)
public class SampleWebSocketEndpoint {

    private final QuarkusWebSocketEndpointBridge bridge;

    public SampleWebSocketEndpoint(QuarkusWebSocketEndpointBridge bridge) {
        this.bridge = bridge;
    }

    @OnOpen
    void onOpen(WebSocketConnection connection) {
        bridge.onOpen(connection);
    }

    @OnTextMessage
    void onMessage(String payload, WebSocketConnection connection) {
        bridge.onTextMessage(payload, connection);
    }

    @OnClose
    void onClose(WebSocketConnection connection) {
        bridge.onClose(connection);
    }
}

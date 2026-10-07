package com.innospots.nexus.service.adapter.test.scenario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;
import com.innospots.nexus.service.contract.status.ServiceStatusCode;

/**
 * 未知消息类型拒绝且连接保持。
 */
public final class WebSocketPermissionScenario implements AdapterScenario {

    @Override
    public void run(AdapterTestTarget target) {
        URI uri = toWebSocketUri(target.resolve(AdapterScenarioPaths.WEBSOCKET));
        CompletableFuture<String> errorCode = new CompletableFuture<>();
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                if (data.toString().contains("ready")) {
                    webSocket.sendText("""
                            {"id":"1","type":"unknown.type","sequence":1,"timestamp":"2026-09-15T10:00:00Z","data":"x"}
                            """, true);
                } else {
                    errorCode.complete(extractCode(data.toString()));
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                errorCode.completeExceptionally(error);
            }
        };
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(uri, listener)
                .join();
        try {
            require(ServiceStatusCode.MESSAGE_TYPE_UNKNOWN.fullCode().equals(errorCode.get(5, TimeUnit.SECONDS)),
                    "expected message type unknown");
        } catch (Exception ex) {
            throw new AssertionError("websocket permission scenario failed", ex);
        }
    }

    private static String extractCode(String body) {
        return Jsons.toMap(body).getOrDefault("code", "").toString();
    }

    private static URI toWebSocketUri(URI httpUri) {
        String scheme = "https".equalsIgnoreCase(httpUri.getScheme()) ? "wss" : "ws";
        return URI.create(scheme + "://" + httpUri.getRawAuthority() + httpUri.getRawPath());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}

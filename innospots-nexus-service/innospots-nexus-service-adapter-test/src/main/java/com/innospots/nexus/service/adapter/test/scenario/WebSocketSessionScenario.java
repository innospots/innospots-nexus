package com.innospots.nexus.service.adapter.test.scenario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * WebSocket 生命周期与 registry send 烟雾测试。
 */
public final class WebSocketSessionScenario implements AdapterScenario {

    @Override
    public void run(AdapterTestTarget target) {
        URI uri = toWebSocketUri(target.resolve(AdapterScenarioPaths.WEBSOCKET));
        CompletableFuture<Void> completed = new CompletableFuture<>();
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                if (data.toString().contains("ready")) {
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                completed.complete(null);
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                completed.completeExceptionally(error);
            }
        };
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(uri, listener)
                .join();
        try {
            completed.get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new AssertionError("websocket session did not complete", ex);
        }
    }

    private static URI toWebSocketUri(URI httpUri) {
        String scheme = "https".equalsIgnoreCase(httpUri.getScheme()) ? "wss" : "ws";
        return URI.create(scheme + "://" + httpUri.getRawAuthority() + httpUri.getRawPath());
    }
}

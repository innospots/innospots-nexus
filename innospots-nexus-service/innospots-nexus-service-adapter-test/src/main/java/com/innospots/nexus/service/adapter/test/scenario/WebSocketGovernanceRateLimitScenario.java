package com.innospots.nexus.service.adapter.test.scenario;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.adapter.test.fixture.AdapterWebSocketFixtures;
import com.innospots.nexus.service.adapter.test.support.AdapterTestTarget;

/**
 * 入站 WebSocket 消息经 {@code InvocationEngine} 限流时第二条返回 error envelope。
 */
public final class WebSocketGovernanceRateLimitScenario implements AdapterScenario {

    @Override
    public void run(AdapterTestTarget target) {
        URI uri = toWebSocketUri(target.resolve(AdapterScenarioPaths.WEBSOCKET));
        CompletableFuture<String> limitCode = new CompletableFuture<>();
        AtomicInteger probeSends = new AtomicInteger();
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                String body = data.toString();
                if (body.contains(AdapterWebSocketFixtures.READY_TYPE)) {
                    sendProbe(webSocket);
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }
                String code = extractCode(body);
                if (NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(code)) {
                    limitCode.complete(code);
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done");
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }
                if (probeSends.get() == 1) {
                    sendProbe(webSocket);
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                limitCode.completeExceptionally(error);
            }

            private void sendProbe(WebSocket webSocket) {
                probeSends.incrementAndGet();
                webSocket.sendText("""
                        {"id":"p%d","type":"%s","sequence":%d,"timestamp":"2026-09-15T10:00:00Z","data":"x"}
                        """.formatted(
                                probeSends.get(),
                                AdapterWebSocketFixtures.RATE_LIMIT_PROBE_TYPE,
                                probeSends.get()),
                        true);
            }
        };
        HttpClient.newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(uri, listener)
                .join();
        try {
            require(NexusStatusCode.LIMIT_EXCEEDED.fullCode().equals(limitCode.get(5, TimeUnit.SECONDS)),
                    "expected websocket rate limit error envelope");
        } catch (Exception ex) {
            throw new AssertionError("websocket governance rate limit scenario failed", ex);
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

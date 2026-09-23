package com.innospots.nexus.spring.service.test.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过 JDK HTTP 客户端消费 SSE，用于集成测试。
 */
public final class SseStreamClient {

    private static final Logger LOG = LoggerFactory.getLogger(SseStreamClient.class);

    private final HttpClient client;

    public SseStreamClient() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
    }

    public SseStreamClient(HttpClient client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    /**
     * GET 并解析 SSE 帧。
     *
     * @param uri 完整 URL
     * @return 捕获结果
     */
    public SseCapture consumeGet(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "text/event-stream")
                .GET()
                .build();
        try {
            HttpResponse<InputStream> response =
                    client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            LOG.info("SSE connect status={} contentType={}",
                    response.statusCode(),
                    response.headers().firstValue("content-type").orElse(""));
            if (response.statusCode() != 200) {
                throw new IllegalStateException("unexpected status " + response.statusCode());
            }
            try (InputStream body = response.body();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(body, StandardCharsets.UTF_8))) {
                return parseFrames(reader);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("SSE consume failed: " + uri, ex);
        }
    }

    private SseCapture parseFrames(BufferedReader reader) throws Exception {
        List<SseFrame> frames = new ArrayList<>();
        String eventName = "message";
        StringBuilder data = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (data.length() > 0) {
                    SseFrame frame = new SseFrame(eventName, data.toString());
                    frames.add(frame);
                    LOG.info("SSE frame event={} data={}", frame.event(), frame.data());
                    data.setLength(0);
                    eventName = "message";
                }
                continue;
            }
            if (line.startsWith("event:")) {
                eventName = line.substring("event:".length()).trim();
            } else if (line.startsWith("data:")) {
                if (data.length() > 0) {
                    data.append('\n');
                }
                data.append(line.substring("data:".length()).trim());
            } else if (line.startsWith(":")) {
                LOG.debug("SSE comment {}", line);
            }
        }
        return new SseCapture(frames);
    }

    /**
     * 单条 SSE 业务帧。
     *
     * @param event 事件名
     * @param data  data 行（通常为 JSON）
     */
    public record SseFrame(String event, String data) {
    }

    /**
     * 一次连接的 SSE 捕获结果。
     *
     * @param frames 按到达顺序的事件帧
     */
    public record SseCapture(List<SseFrame> frames) {

        public SseCapture {
            frames = frames == null ? List.of() : List.copyOf(frames);
        }

        /**
         * 返回指定事件名的 data 列表。
         *
         * @param eventName 事件名
         * @return data 列表
         */
        public List<String> dataByEvent(String eventName) {
            return frames.stream()
                    .filter(frame -> frame.event().equals(eventName))
                    .map(SseFrame::data)
                    .toList();
        }
    }
}

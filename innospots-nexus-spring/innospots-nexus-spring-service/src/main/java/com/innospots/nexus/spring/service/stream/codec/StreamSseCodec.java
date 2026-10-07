package com.innospots.nexus.spring.service.stream.codec;

import java.nio.charset.StandardCharsets;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.stream.encode.NdjsonStreamEncoder;
import com.innospots.nexus.service.stream.event.StreamEvent;
import com.innospots.nexus.service.stream.event.StreamEventType;

/**
 * 将 {@link StreamEvent} 编码为 SSE 帧（与 NDJSON 共用 data 形状）。
 */
public final class StreamSseCodec {

    private StreamSseCodec() {
    }

    /**
     * 构建 MVC {@link SseEmitter} 事件。
     *
     * @param event 流事件
     * @return SSE 事件
     */
    public static SseEmitter.SseEventBuilder toMvcEvent(StreamEvent<?> event) {
        Checks.notNull(event, "event");
        if (StreamEventType.HEARTBEAT.wireName().equals(event.type())) {
            return SseEmitter.event().comment("heartbeat");
        }
        String data = encodeDataJson(event);
        return SseEmitter.event()
                .id(event.id())
                .name(event.type())
                .data(data);
    }

    /**
     * 构建 WebFlux {@link ServerSentEvent}。
     *
     * @param event 流事件
     * @return SSE 事件
     */
    public static ServerSentEvent<String> toReactiveEvent(StreamEvent<?> event) {
        Checks.notNull(event, "event");
        if (StreamEventType.HEARTBEAT.wireName().equals(event.type())) {
            return ServerSentEvent.<String>builder().comment("heartbeat").build();
        }
        return ServerSentEvent.builder(encodeDataJson(event))
                .id(event.id())
                .event(event.type())
                .build();
    }

    /**
     * 编码为 SSE 文本帧（用于 WebFlux {@code DataBuffer} 写出）。
     *
     * @param event 流事件
     * @return UTF-8 文本帧
     */
    public static byte[] toSseFrameBytes(StreamEvent<?> event) {
        return formatSseFrame(event).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 编码为 SSE 文本帧。
     *
     * @param event 流事件
     * @return 帧文本
     */
    public static String formatSseFrame(StreamEvent<?> event) {
        Checks.notNull(event, "event");
        if (StreamEventType.HEARTBEAT.wireName().equals(event.type())) {
            return ": heartbeat\n\n";
        }
        return "id: " + event.id() + "\n"
                + "event: " + event.type() + "\n"
                + "data: " + encodeDataJson(event) + "\n\n";
    }

    private static String encodeDataJson(StreamEvent<?> event) {
        String line = NdjsonStreamEncoder.encodeLine(event);
        if (line.endsWith("\n")) {
            return line.substring(0, line.length() - 1);
        }
        return line;
    }
}

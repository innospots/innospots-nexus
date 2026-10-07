package com.innospots.nexus.service.stream.encode;

import java.util.LinkedHashMap;
import java.util.Map;

import com.innospots.nexus.base.json.Jsons;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.stream.event.StreamEvent;

/**
 * NDJSON 线格式编码器。
 */
public final class NdjsonStreamEncoder {

    public static final String MEDIA_TYPE = "application/x-ndjson";

    private NdjsonStreamEncoder() {
    }

    /**
     * 将事件编码为单行 JSON。
     *
     * @param event 流事件
     * @return 带换行符的单行
     */
    public static String encodeLine(StreamEvent<?> event) {
        Checks.notNull(event, "event");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", event.id());
        payload.put("sequence", event.sequence());
        payload.put("type", event.type());
        payload.put("timestamp", event.timestamp().toString());
        payload.put("data", event.data());
        return Jsons.toJson(payload) + "\n";
    }
}

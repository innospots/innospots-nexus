package com.innospots.nexus.service.stream.encode;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.service.stream.event.StreamEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NDJSON 编码器契约测试。
 */
class NdjsonStreamEncoderTest {

    @Test
    void encodesSingleLineJsonWithTrailingNewline() {
        StreamEvent<String> event = new StreamEvent<>("evt-1", 1L, "message", Instant.parse("2026-09-15T10:00:00Z"), "hello");
        String line = NdjsonStreamEncoder.encodeLine(event);
        assertThat(line).endsWith("\n");
        assertThat(line).contains("\"type\":\"message\"");
        assertThat(line).contains("\"data\":\"hello\"");
        assertThat(NdjsonStreamEncoder.MEDIA_TYPE).isEqualTo("application/x-ndjson");
    }
}

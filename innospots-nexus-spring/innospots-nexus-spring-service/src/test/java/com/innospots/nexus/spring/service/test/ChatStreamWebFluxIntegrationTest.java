package com.innospots.nexus.spring.service.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import com.innospots.nexus.spring.service.test.fixture.ChatStreamEvent;
import com.innospots.nexus.spring.service.test.fixture.ChatStreamFixturePaths;
import com.innospots.nexus.spring.service.test.support.SseStreamClient;
import com.innospots.nexus.spring.service.test.support.StreamEventSseParser;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 在嵌入式 WebFlux 宿主上验证 Chat 风格 {@code StreamSession} SSE 输出。
 */
@SpringBootTest(classes = WebFluxTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("webflux")
class ChatStreamWebFluxIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ChatStreamWebFluxIntegrationTest.class);

    private static final String EXPECTED_REPLY = "你好，我是 Nexus 助手";

    @LocalServerPort
    private int port;

    @Test
    void streamsChatLikeAssistantTokensOverSse() {
        URI uri = URI.create("http://127.0.0.1:" + port + ChatStreamFixturePaths.STREAM_CHAT);
        LOG.info("webflux chat stream test start uri={}", uri);

        SseStreamClient.SseCapture capture = new SseStreamClient().consumeGet(uri);

        List<String> deltaPayloads = capture.dataByEvent("message.delta");
        assertThat(deltaPayloads).hasSize(6);
        LOG.info("received message.delta count={}", deltaPayloads.size());

        StringBuilder assembled = new StringBuilder();
        for (String payload : deltaPayloads) {
            ChatStreamEvent event = StreamEventSseParser.parsePayload(payload, ChatStreamEvent.class);
            assembled.append(event.content());
            LOG.info("chat delta token={}", event.content());
        }
        assertThat(assembled.toString()).isEqualTo(EXPECTED_REPLY);

        List<ChatStreamEvent> terminalMessages = new ArrayList<>();
        for (String payload : capture.dataByEvent("message")) {
            terminalMessages.add(StreamEventSseParser.parsePayload(payload, ChatStreamEvent.class));
        }
        assertThat(terminalMessages).anyMatch(ChatStreamEvent::finished);

        assertThat(capture.dataByEvent("error")).isEmpty();
        assertThat(capture.frames().get(capture.frames().size() - 1).event()).isEqualTo("message");
        LOG.info("webflux chat stream test done frames={}", capture.frames().size());
    }
}

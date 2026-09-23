package com.innospots.nexus.spring.service.test.fixture;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.service.stream.session.StreamManager;
import com.innospots.nexus.service.stream.session.StreamSession;

/**
 * 模拟 Chat 增量输出的 WebFlux 流式夹具（经 {@link StreamSession} 写 SSE）。
 */
@RestController
@Profile("webflux")
public class SampleWebFluxChatStreamResource {

    private static final String[] ASSISTANT_TOKENS = {"你", "好", "，", "我是", " Nexus", " 助手"};

    private final StreamManager streamManager;

    public SampleWebFluxChatStreamResource(StreamManager streamManager) {
        this.streamManager = streamManager;
    }

    @GetMapping(value = ChatStreamFixturePaths.STREAM_CHAT, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamSession<ChatStreamEvent> streamChat() {
        StreamSession<ChatStreamEvent> session =
                (StreamSession<ChatStreamEvent>) streamManager.open(ChatStreamEvent.class);
        Thread worker = new Thread(() -> pumpAssistantReply(session), "fixture-webflux-chat-stream");
        worker.setDaemon(true);
        worker.start();
        return session;
    }

    private static void pumpAssistantReply(StreamSession<ChatStreamEvent> session) {
        try {
            for (String token : ASSISTANT_TOKENS) {
                if (session.isCancelled()) {
                    return;
                }
                session.emit("message.delta", new ChatStreamEvent("assistant", token, false))
                        .toCompletableFuture()
                        .join();
                Thread.sleep(15L);
            }
            if (session.isCancelled()) {
                return;
            }
            session.emit("message", new ChatStreamEvent("assistant", "", true))
                    .toCompletableFuture()
                    .join();
            session.complete().toCompletableFuture().join();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR, ex));
        } catch (Exception ex) {
            session.fail(NexusException.build(NexusStatusCode.SYSTEM_ERROR, ex));
        }
    }
}

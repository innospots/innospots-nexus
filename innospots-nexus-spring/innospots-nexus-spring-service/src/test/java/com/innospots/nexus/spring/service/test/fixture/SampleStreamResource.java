package com.innospots.nexus.spring.service.test.fixture;

import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.stream.session.StreamManager;
import com.innospots.nexus.service.stream.session.StreamSession;
import com.innospots.nexus.spring.service.http.mvc.ServiceServletFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * adapter-test 流式夹具端点。
 */
@RestController
@Profile("!webflux")
public class SampleStreamResource {

    private final ServiceContextAccessor contextAccessor;
    private final StreamManager streamManager;

    public SampleStreamResource(ServiceContextAccessor contextAccessor, StreamManager streamManager) {
        this.contextAccessor = contextAccessor;
        this.streamManager = streamManager;
    }

    @GetMapping(value = AdapterScenarioPaths.STREAM_SSE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamSession<String> streamSse() {
        StreamSession<String> session = (StreamSession<String>) streamManager.open(String.class);
        session.emit("message", "hello");
        session.emit("complete", "done");
        session.complete();
        return session;
    }

    @GetMapping(value = AdapterScenarioPaths.STREAM_CANCEL, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCancel(HttpServletRequest request) {
        CancellationToken cancellation = contextAccessor.requireCurrent().cancellation();
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(1).toMillis());
        Thread worker = new Thread(() -> {
            try {
                for (int index = 0; index < 100; index++) {
                    if (cancellation.isCancelled()) {
                        break;
                    }
                    emitter.send(SseEmitter.event().name("message").data("chunk-" + index));
                    Thread.sleep(200L);
                }
                emitter.complete();
            } catch (IOException | InterruptedException ex) {
                ServiceServletFilter.cancelIfClientDisconnected(request);
                emitter.completeWithError(ex);
                Thread.currentThread().interrupt();
            }
        }, "adapter-stream-cancel");
        worker.setDaemon(true);
        worker.start();
        return emitter;
    }
}

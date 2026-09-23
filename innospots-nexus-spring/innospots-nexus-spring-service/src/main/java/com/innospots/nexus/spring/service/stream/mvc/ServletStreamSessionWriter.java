package com.innospots.nexus.spring.service.stream.mvc;

import java.io.IOException;
import java.util.concurrent.Flow;

import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.stream.config.StreamConfig;
import com.innospots.nexus.service.stream.event.StreamEvent;
import com.innospots.nexus.service.stream.session.StreamSession;
import com.innospots.nexus.spring.service.stream.codec.StreamSseCodec;
import com.innospots.nexus.spring.service.http.mvc.ServiceServletFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 将 {@link StreamSession} 订阅结果写入 Servlet SSE 响应。
 */
public final class ServletStreamSessionWriter {

    private final StreamConfig streamConfig;

    /**
     * 创建写入器。
     *
     * @param streamConfig 流配置
     */
    public ServletStreamSessionWriter(StreamConfig streamConfig) {
        this.streamConfig = Checks.notNull(streamConfig, "streamConfig");
    }

    /**
     * 写回 SSE 并返回 {@link SseEmitter}（由 Spring MVC 异步持有连接）。
     *
     * @param request      当前请求
     * @param response     当前响应
     * @param session      流会话
     * @param cancellation 取消令牌
     * @return SSE 发射器
     */
    public SseEmitter write(
            HttpServletRequest request,
            HttpServletResponse response,
            StreamSession<?> session,
            CancellationToken cancellation) {
        Checks.notNull(request, "request");
        Checks.notNull(response, "response");
        Checks.notNull(session, "session");
        Checks.notNull(cancellation, "cancellation");
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        long timeoutMillis = Math.max(streamConfig.ttl().toMillis(), streamConfig.idleTimeout().toMillis());
        SseEmitter emitter = new SseEmitter(timeoutMillis);
        emitter.onCompletion(() -> ServiceServletFilter.cancelIfClientDisconnected(request));
        emitter.onTimeout(() -> ServiceServletFilter.cancelIfClientDisconnected(request));
        emitter.onError(ex -> ServiceServletFilter.cancelIfClientDisconnected(request));
        Thread worker = new Thread(() -> pump(session, cancellation, request, emitter), "stream-sse-" + session.sessionId());
        worker.setDaemon(true);
        worker.start();
        return emitter;
    }

    private void pump(
            StreamSession<?> session,
            CancellationToken cancellation,
            HttpServletRequest request,
            SseEmitter emitter) {
        session.publisher().subscribe(new Flow.Subscriber<StreamEvent<?>>() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(StreamEvent<?> item) {
                if (cancellation.isCancelled()) {
                    subscription.cancel();
                    emitter.complete();
                    return;
                }
                try {
                    emitter.send(StreamSseCodec.toMvcEvent(item));
                    subscription.request(1);
                } catch (IOException ex) {
                    ServiceServletFilter.cancelIfClientDisconnected(request);
                    subscription.cancel();
                    emitter.completeWithError(ex);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                emitter.completeWithError(throwable);
            }

            @Override
            public void onComplete() {
                emitter.complete();
            }
        });
    }
}

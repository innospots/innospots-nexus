package com.innospots.nexus.spring.service.stream.webflux;

import org.reactivestreams.FlowAdapters;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.stream.session.StreamSession;
import com.innospots.nexus.spring.service.stream.codec.StreamSseCodec;
import com.innospots.nexus.spring.service.http.webflux.ServiceReactiveExchangeHolder;
import com.innospots.nexus.spring.service.http.webflux.ServiceWebFilter;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 将 {@link StreamSession} 写为 WebFlux SSE 响应。
 */
public final class ReactiveStreamSessionWriter {

    /**
     * 写回 SSE 响应体。
     *
     * @param exchange     exchange
     * @param session      流会话
     * @param cancellation 取消令牌
     * @return 完成信号
     */
    public Mono<Void> write(ServerWebExchange exchange, StreamSession<?> session, CancellationToken cancellation) {
        Checks.notNull(exchange, "exchange");
        Checks.notNull(session, "session");
        Checks.notNull(cancellation, "cancellation");
        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
        Flux<org.springframework.core.io.buffer.DataBuffer> body = Flux.from(
                        FlowAdapters.toPublisher(session.publisher()))
                .takeWhile(event -> !cancellation.isCancelled())
                .map(event -> exchange.getResponse().bufferFactory().wrap(StreamSseCodec.toSseFrameBytes(event)))
                .doOnCancel(() -> {
                    ServerWebExchange holder = ServiceReactiveExchangeHolder.get();
                    if (holder != null) {
                        ServiceWebFilter.cancelIfClientDisconnected(holder);
                    }
                });
        return exchange.getResponse().writeWith(body);
    }
}

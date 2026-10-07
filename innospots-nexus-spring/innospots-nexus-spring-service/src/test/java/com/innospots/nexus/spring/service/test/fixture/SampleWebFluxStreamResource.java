package com.innospots.nexus.spring.service.test.fixture;

import java.time.Duration;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.service.adapter.test.scenario.AdapterScenarioPaths;
import com.innospots.nexus.service.contract.cancellation.CancellationToken;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.spring.service.http.webflux.ServiceReactiveExchangeHolder;
import com.innospots.nexus.spring.service.http.webflux.ServiceWebFilter;

import reactor.core.publisher.Flux;

/**
 * adapter-test WebFlux 流式夹具端点。
 */
@RestController
@Profile("webflux")
public class SampleWebFluxStreamResource {

    private final ServiceContextAccessor contextAccessor;

    public SampleWebFluxStreamResource(ServiceContextAccessor contextAccessor) {
        this.contextAccessor = contextAccessor;
    }

    @GetMapping(value = AdapterScenarioPaths.STREAM_SSE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamSse() {
        return Flux.just(
                ServerSentEvent.builder("hello").event("message").build(),
                ServerSentEvent.builder("done").event("complete").build());
    }

    @GetMapping(value = AdapterScenarioPaths.STREAM_CANCEL, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamCancel() {
        CancellationToken cancellation = contextAccessor.requireCurrent().cancellation();
        ServerWebExchange exchange = ServiceReactiveExchangeHolder.get();
        return Flux.interval(Duration.ofMillis(200))
                .take(100)
                .takeWhile(index -> !cancellation.isCancelled())
                .map(index -> ServerSentEvent.builder("chunk-" + index).event("message").build())
                .doOnCancel(() -> {
                    if (exchange != null) {
                        ServiceWebFilter.cancelIfClientDisconnected(exchange);
                    }
                });
    }
}

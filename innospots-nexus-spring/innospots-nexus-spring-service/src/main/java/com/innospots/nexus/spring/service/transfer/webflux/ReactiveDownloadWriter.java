package com.innospots.nexus.spring.service.transfer.webflux;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.BinarySource;
import com.innospots.nexus.service.transfer.content.BinarySources;
import com.innospots.nexus.service.transfer.download.DownloadPlan;
import com.innospots.nexus.service.transfer.download.DownloadRequests;
import com.innospots.nexus.service.transfer.download.DownloadResource;
import com.innospots.nexus.service.transfer.download.DownloadTransferSupport;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 将 {@link DownloadResource} 写入 WebFlux 响应。
 */
public final class ReactiveDownloadWriter {

    private final DownloadTransferSupport transferSupport;

    /**
     * 创建写入器。
     *
     * @param transferSupport 下载执行器
     */
    public ReactiveDownloadWriter(DownloadTransferSupport transferSupport) {
        this.transferSupport = Checks.notNull(transferSupport, "transferSupport");
    }

    /**
     * 写回下载响应。
     *
     * @param exchange WebFlux exchange
     * @param resource 下载资源
     * @return 完成信号
     */
    public Mono<Void> write(ServerWebExchange exchange, DownloadResource resource) {
        Checks.notNull(exchange, "exchange");
        Checks.notNull(resource, "resource");
        ServerHttpRequest request = exchange.getRequest();
        DownloadTransferSupport.PreparedDownload prepared = transferSupport.prepare(
                DownloadRequests.from(request.getMethod().name(), reactiveHeaders(request)),
                resource);
        applyPlan(exchange.getResponse(), resource, prepared.plan());
        if (prepared.body() == null) {
            return exchange.getResponse().setComplete();
        }
        Flux<DataBuffer> body = bodyFlux(exchange, prepared.body());
        return exchange.getResponse().writeWith(body);
    }

    private static Flux<DataBuffer> bodyFlux(ServerWebExchange exchange, BinarySource source) {
        return Flux.create(sink -> {
            try {
                BinarySources.drain(source, buffer -> {
                    if (sink.isCancelled()) {
                        return;
                    }
                    sink.next(exchange.getResponse().bufferFactory().wrap(buffer));
                });
                sink.complete();
            } catch (RuntimeException ex) {
                sink.error(ex);
            } finally {
                source.close();
            }
        });
    }

    private static Map<String, List<String>> reactiveHeaders(ServerHttpRequest request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), List.copyOf(values)));
        return headers;
    }

    private static void applyPlan(ServerHttpResponse response, DownloadResource resource, DownloadPlan plan) {
        response.setRawStatusCode(plan.httpStatus());
        plan.headers().forEach(response.getHeaders()::add);
        if (resource.filename() != null && !resource.filename().isBlank()) {
            response.getHeaders().add("Content-Disposition", "attachment; filename=\"" + resource.filename() + "\"");
        }
    }
}

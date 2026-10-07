package com.innospots.nexus.spring.service.transfer.webflux;

import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.download.DownloadResource;

import reactor.core.publisher.Mono;

/**
 * WebFlux {@link DownloadResource} 结果处理器。
 */
public final class ServiceDownloadHandlerResultHandler implements HandlerResultHandler {

    private final ReactiveDownloadWriter downloadWriter;

    /**
     * 创建结果处理器。
     *
     * @param downloadWriter 响应式下载写入器
     */
    public ServiceDownloadHandlerResultHandler(ReactiveDownloadWriter downloadWriter) {
        this.downloadWriter = Checks.notNull(downloadWriter, "downloadWriter");
    }

    @Override
    public boolean supports(HandlerResult result) {
        return result.getReturnValue() instanceof DownloadResource;
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        DownloadResource resource = (DownloadResource) result.getReturnValue();
        return downloadWriter.write(exchange, resource);
    }
}

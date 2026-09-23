package com.innospots.nexus.spring.service.stream.webflux;

import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.server.ServerWebExchange;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.stream.session.StreamSession;

import reactor.core.publisher.Mono;

/**
 * WebFlux 下将 {@link StreamSession} 写为 SSE。
 */
public final class ReactiveStreamSessionResultHandler implements HandlerResultHandler {

    private final ReactiveStreamSessionWriter streamWriter;
    private final ServiceContextAccessor contextAccessor;

    /**
     * 创建结果处理器。
     *
     * @param streamWriter    流写入器
     * @param contextAccessor 上下文访问器
     */
    public ReactiveStreamSessionResultHandler(
            ReactiveStreamSessionWriter streamWriter,
            ServiceContextAccessor contextAccessor) {
        this.streamWriter = Checks.notNull(streamWriter, "streamWriter");
        this.contextAccessor = Checks.notNull(contextAccessor, "contextAccessor");
    }

    @Override
    public boolean supports(HandlerResult result) {
        return result.getReturnValue() instanceof StreamSession;
    }

    @Override
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        StreamSession<?> session = (StreamSession<?>) result.getReturnValue();
        return streamWriter.write(
                exchange,
                session,
                contextAccessor.requireCurrent().cancellation());
    }
}

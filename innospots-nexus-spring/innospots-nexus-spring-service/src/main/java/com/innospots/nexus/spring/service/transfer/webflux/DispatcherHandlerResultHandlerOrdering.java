package com.innospots.nexus.spring.service.transfer.webflux;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerResultHandler;

import com.innospots.nexus.base.util.Checks;

/**
 * 将指定 {@link HandlerResultHandler} 置于 {@link DispatcherHandler} 链首。
 *
 * <p>Spring Boot 默认 {@code ResponseBodyResultHandler} 可序列化 {@code DownloadResource}，
 * 须在链上优先于 JSON 写回处理器。</p>
 */
final class DispatcherHandlerResultHandlerOrdering {

    private DispatcherHandlerResultHandlerOrdering() {
    }

    static void prepend(DispatcherHandler dispatcherHandler, HandlerResultHandler handler) {
        Checks.notNull(dispatcherHandler, "dispatcherHandler");
        Checks.notNull(handler, "handler");
        try {
            Field field = DispatcherHandler.class.getDeclaredField("resultHandlers");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<HandlerResultHandler> handlers =
                    new ArrayList<>((List<HandlerResultHandler>) field.get(dispatcherHandler));
            handlers.remove(handler);
            handlers.addFirst(handler);
            field.set(dispatcherHandler, handlers);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("failed to reorder WebFlux result handlers", ex);
        }
    }
}

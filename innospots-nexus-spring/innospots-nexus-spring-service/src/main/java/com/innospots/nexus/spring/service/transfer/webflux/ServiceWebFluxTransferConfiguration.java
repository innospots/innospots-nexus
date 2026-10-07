package com.innospots.nexus.spring.service.transfer.webflux;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerResultHandler;

/**
 * 注册 WebFlux 下载结果处理器。
 */
@Configuration
public class ServiceWebFluxTransferConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    ServiceDownloadHandlerResultHandler serviceDownloadHandlerResultHandler(
            ReactiveDownloadWriter serviceReactiveDownloadWriter) {
        return new ServiceDownloadHandlerResultHandler(serviceReactiveDownloadWriter);
    }

    @Bean
    SmartInitializingSingleton serviceDownloadHandlerResultHandlerOrdering(
            DispatcherHandler dispatcherHandler,
            ServiceDownloadHandlerResultHandler serviceDownloadHandlerResultHandler) {
        return () -> DispatcherHandlerResultHandlerOrdering.prepend(
                dispatcherHandler,
                serviceDownloadHandlerResultHandler);
    }
}

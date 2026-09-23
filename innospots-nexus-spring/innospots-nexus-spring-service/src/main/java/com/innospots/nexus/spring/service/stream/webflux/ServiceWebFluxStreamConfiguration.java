package com.innospots.nexus.spring.service.stream.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.reactive.HandlerResultHandler;

import com.innospots.nexus.service.contract.context.ServiceContextAccessor;

/**
 * 注册 WebFlux {@link StreamSession} 结果处理器。
 */
@Configuration
public class ServiceWebFluxStreamConfiguration {

    @Bean
    ReactiveStreamSessionWriter serviceReactiveStreamSessionWriter() {
        return new ReactiveStreamSessionWriter();
    }

    @Bean
    @Order(0)
    HandlerResultHandler serviceStreamResultHandler(
            ReactiveStreamSessionWriter serviceReactiveStreamSessionWriter,
            ServiceContextAccessor contextAccessor) {
        return new ReactiveStreamSessionResultHandler(serviceReactiveStreamSessionWriter, contextAccessor);
    }
}

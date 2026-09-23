package com.innospots.nexus.spring.service.http.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;
import com.innospots.nexus.spring.service.core.ServiceProperties;
import com.innospots.nexus.spring.service.http.mvc.ServiceRequestLifecycleAccessor;
import com.innospots.nexus.spring.service.http.mvc.ServiceTransportSupport;
import com.innospots.nexus.service.http.error.HttpErrorMapper;

/**
 * 注册 WebFlux 过滤器。
 */
@Configuration
public class ServiceWebFluxConfiguration {

    @Bean
    ServiceWebExceptionHandler serviceWebExceptionHandler(
            HttpErrorMapper serviceHttpErrorMapper,
            ServiceProperties serviceProperties,
            ServiceRequestLifecycleAccessor serviceRequestLifecycleAccessor) {
        return new ServiceWebExceptionHandler(
                serviceHttpErrorMapper,
                serviceProperties,
                serviceRequestLifecycleAccessor);
    }

    @Bean
    ServiceWebFilter serviceWebFilter(
            ServiceTransportSupport serviceTransportSupport,
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            AccessLogWriter serviceAccessLogWriter,
            MdcContextBridge serviceMdcContextBridge) {
        return new ServiceWebFilter(
                serviceTransportSupport,
                serviceThreadBoundServiceContext,
                serviceAccessLogWriter,
                serviceMdcContextBridge);
    }
}

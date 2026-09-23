package com.innospots.nexus.spring.service.http.mvc;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.innospots.nexus.service.observability.logging.AccessLogWriter;
import com.innospots.nexus.service.observability.logging.MdcContextBridge;
import com.innospots.nexus.service.runtime.context.ThreadBoundServiceContext;

/**
 * 注册 Servlet 过滤器。
 */
@Configuration
public class ServiceMvcConfiguration {

    @Bean
    ServiceServletFilter serviceServletFilter(
            ServiceTransportSupport serviceTransportSupport,
            ThreadBoundServiceContext serviceThreadBoundServiceContext,
            AccessLogWriter serviceAccessLogWriter,
            MdcContextBridge serviceMdcContextBridge) {
        return new ServiceServletFilter(
                serviceTransportSupport,
                serviceThreadBoundServiceContext,
                serviceAccessLogWriter,
                serviceMdcContextBridge);
    }

    @Bean
    FilterRegistrationBean<ServiceServletFilter> serviceServletFilterRegistration(ServiceServletFilter serviceServletFilter) {
        FilterRegistrationBean<ServiceServletFilter> registration = new FilterRegistrationBean<>(serviceServletFilter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
        registration.addUrlPatterns("/*");
        return registration;
    }
}

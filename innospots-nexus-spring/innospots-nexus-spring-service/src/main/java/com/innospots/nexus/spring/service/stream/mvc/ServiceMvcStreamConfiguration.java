package com.innospots.nexus.spring.service.stream.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.innospots.nexus.service.contract.context.ServiceContextAccessor;
import com.innospots.nexus.service.stream.config.StreamConfig;

/**
 * 注册 MVC {@link StreamSession} 返回值处理器。
 */
@Configuration
public class ServiceMvcStreamConfiguration {

    @Bean
    static ServletStreamSessionWriter serviceServletStreamSessionWriter(StreamConfig serviceStreamConfig) {
        return new ServletStreamSessionWriter(serviceStreamConfig);
    }

    @Bean
    ServiceStreamReturnValueHandler serviceStreamReturnValueHandler(
            ServletStreamSessionWriter streamWriter,
            ServiceContextAccessor contextAccessor) {
        return new ServiceStreamReturnValueHandler(streamWriter, contextAccessor);
    }

    @Bean
    SmartInitializingSingleton serviceStreamReturnValueHandlerOrdering(
            RequestMappingHandlerAdapter requestMappingHandlerAdapter,
            ServiceStreamReturnValueHandler streamReturnValueHandler) {
        return () -> {
            List<HandlerMethodReturnValueHandler> handlers =
                    new ArrayList<>(requestMappingHandlerAdapter.getReturnValueHandlers());
            handlers.remove(streamReturnValueHandler);
            handlers.addFirst(streamReturnValueHandler);
            requestMappingHandlerAdapter.setReturnValueHandlers(handlers);
        };
    }
}

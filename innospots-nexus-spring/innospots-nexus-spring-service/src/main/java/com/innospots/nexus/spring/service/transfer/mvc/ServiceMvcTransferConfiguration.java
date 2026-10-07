package com.innospots.nexus.spring.service.transfer.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * 注册 MVC 下载返回值处理器。
 */
@Configuration
public class ServiceMvcTransferConfiguration {

    @Bean
    ServiceDownloadReturnValueHandler serviceDownloadReturnValueHandler(
            ServletDownloadWriter serviceServletDownloadWriter) {
        return new ServiceDownloadReturnValueHandler(serviceServletDownloadWriter);
    }

    @Bean
    SmartInitializingSingleton serviceDownloadReturnValueHandlerOrdering(
            RequestMappingHandlerAdapter requestMappingHandlerAdapter,
            ServiceDownloadReturnValueHandler serviceDownloadReturnValueHandler) {
        return () -> {
            List<HandlerMethodReturnValueHandler> handlers =
                    new ArrayList<>(requestMappingHandlerAdapter.getReturnValueHandlers());
            handlers.remove(serviceDownloadReturnValueHandler);
            handlers.addFirst(serviceDownloadReturnValueHandler);
            requestMappingHandlerAdapter.setReturnValueHandlers(handlers);
        };
    }
}

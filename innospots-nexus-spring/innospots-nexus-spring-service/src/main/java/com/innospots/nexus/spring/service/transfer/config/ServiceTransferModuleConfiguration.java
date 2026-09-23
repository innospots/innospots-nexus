package com.innospots.nexus.spring.service.transfer.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.core.ServiceCoreConfiguration;
import com.innospots.nexus.spring.service.transfer.mvc.ServiceMvcTransferConfiguration;
import com.innospots.nexus.spring.service.transfer.webflux.ServiceWebFluxTransferConfiguration;

/**
 * 文件下载（{@code DownloadResource}）Spring 装配，按 Web 类型二选一生效。
 *
 * @see EnableNexusServiceTransfer
 */
public final class ServiceTransferModuleConfiguration {

    private ServiceTransferModuleConfiguration() {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @Import({
            ServiceCoreConfiguration.class,
            ServiceTransferConfiguration.class,
            ServiceMvcTransferConfiguration.class
    })
    public static class ServletTransferModuleConfiguration {
    }

    @Configuration
    @ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass(name = "org.springframework.web.reactive.DispatcherHandler")
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @Import({
            ServiceCoreConfiguration.class,
            ServiceTransferConfiguration.class,
            ServiceWebFluxTransferConfiguration.class
    })
    public static class ReactiveTransferModuleConfiguration {
    }
}

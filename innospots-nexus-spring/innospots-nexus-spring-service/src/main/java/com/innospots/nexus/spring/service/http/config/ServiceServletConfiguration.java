package com.innospots.nexus.spring.service.http.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.service.core.ServiceCoreConfiguration;
import com.innospots.nexus.spring.service.core.ServiceProperties;
import com.innospots.nexus.spring.service.http.mvc.ServiceExceptionAdvice;
import com.innospots.nexus.spring.service.http.mvc.ServiceMvcConfiguration;

/**
 * 中立运行时与 Spring MVC 装配。
 *
 * @see EnableNexusServiceHttp
 */
@Configuration
@ConditionalOnProperty(prefix = "service", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ServiceProperties.class)
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
        ServiceCoreConfiguration.class,
        ServiceMvcConfiguration.class,
        ServiceExceptionAdvice.class
})
public class ServiceServletConfiguration {
}

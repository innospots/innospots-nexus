package com.innospots.nexus.sample.spring.platform.jaxrs.web.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.innospots.nexus.spring.console.config.ConsoleJaxRsWebConfiguration;
import com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration;

/**
 * 仅装配 Jersey Web 过滤器/异常映射与测试用 JAX-RS 资源。
 */
@SpringBootApplication(scanBasePackageClasses = ConsoleJaxRsWebTestApplication.class)
@Import({
        ConsoleJaxRsWebConfiguration.class,
        NexusJaxRsConfiguration.class,
        ConsoleJaxRsWebTestConfiguration.class
})
public class ConsoleJaxRsWebTestApplication {
}

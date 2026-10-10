package com.innospots.nexus.spring.core.jaxrs;

import jakarta.ws.rs.ext.ExceptionMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.core.jaxrs.exception.JaxRsExceptionSupport;
import com.innospots.nexus.core.jaxrs.exception.NexusExceptionMapper;
import com.innospots.nexus.core.jaxrs.exception.ThrowableExceptionMapper;
import com.innospots.nexus.core.jaxrs.exception.WebApplicationExceptionMapper;

/**
 * 通用 Jakarta REST 异常映射的 Spring 装配（与权限、跨域、console/platform 业务域无关）。
 *
 * <p>将 {@code com.innospots.nexus.core.jaxrs.exception} 下的 {@link JaxRsExceptionSupport}
 * 与三个 {@link jakarta.ws.rs.ext.ExceptionMapper} 注册为 Bean，并通过
 * {@link ResourceConfigCustomizer} 挂载到宿主 Jersey {@code ResourceConfig}；
 * 各业务宿主经由 {@link com.innospots.nexus.spring.core.bootstrap.EnableNexusSimpleBootstrap}
 * 或 {@link com.innospots.nexus.spring.core.bootstrap.EnableNexusHostBootstrap} 复用，
 * 不必在各自装配中重复声明。</p>
 *
 * @see JaxRsExceptionSupport
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({ResourceConfigCustomizer.class, ExceptionMapper.class})
public class NexusJaxRsExceptionConfiguration {

    /** 将 {@link com.innospots.nexus.base.exception.NexusException} 转为 legacy {@code R} 响应。 */
    @Bean
    JaxRsExceptionSupport jaxRsExceptionSupport() {
        return new JaxRsExceptionSupport();
    }

    /** 映射 {@link com.innospots.nexus.base.exception.NexusException}。 */
    @Bean
    NexusExceptionMapper nexusExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        return new NexusExceptionMapper(exceptionSupport);
    }

    /** 未捕获异常的兜底映射。 */
    @Bean
    ThrowableExceptionMapper throwableExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        return new ThrowableExceptionMapper(exceptionSupport);
    }

    /** 映射 {@link jakarta.ws.rs.WebApplicationException}，避免 404 等被当作系统错误。 */
    @Bean
    WebApplicationExceptionMapper webApplicationExceptionMapper(JaxRsExceptionSupport exceptionSupport) {
        return new WebApplicationExceptionMapper(exceptionSupport);
    }

    /** 将异常映射注册到宿主 Jersey {@code ResourceConfig}（与业务 {@code @Path} 资源并列）。 */
    @Bean
    ResourceConfigCustomizer nexusJaxRsExceptionResourceConfigCustomizer(
            NexusExceptionMapper nexusExceptionMapper,
            ThrowableExceptionMapper throwableExceptionMapper,
            WebApplicationExceptionMapper webApplicationExceptionMapper) {
        return resourceConfig -> {
            resourceConfig.register(nexusExceptionMapper);
            resourceConfig.register(webApplicationExceptionMapper);
            resourceConfig.register(throwableExceptionMapper);
        };
    }
}

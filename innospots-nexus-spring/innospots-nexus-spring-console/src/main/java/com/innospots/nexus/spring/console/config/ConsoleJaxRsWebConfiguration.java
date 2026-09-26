package com.innospots.nexus.spring.console.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jersey.autoconfigure.ResourceConfigCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;
import com.innospots.nexus.spring.console.jaxrs.exception.ConsoleJaxRsExceptionSupport;
import com.innospots.nexus.spring.console.jaxrs.exception.ConsoleNexusExceptionMapper;
import com.innospots.nexus.spring.console.jaxrs.exception.ConsoleThrowableExceptionMapper;
import com.innospots.nexus.spring.console.jaxrs.filter.ConsoleAuthenticationFilter;
import com.innospots.nexus.spring.console.jaxrs.filter.ConsoleCorsFilter;
import com.innospots.nexus.spring.console.jaxrs.filter.ConsoleDevSessionFilter;
import com.innospots.nexus.spring.console.jaxrs.filter.ConsolePagePermissionFilter;
import com.innospots.nexus.spring.console.jaxrs.filter.ConsoleRequestContextFilter;

/**
 * 管理控制台 Jersey 横切过滤器与异常映射的 Spring 装配。
 *
 * <p>在 Servlet + Jersey Filter 模式下将过滤器、{@link jakarta.ws.rs.ext.ExceptionMapper}
 * 注册到 {@link org.glassfish.jersey.server.ResourceConfig}；行为由 {@link ConsoleWebProperties} 驱动。</p>
 *
 * @author Smars
 * @date 2026/09/25
 * @see ConsoleWebProperties
 * @see com.innospots.nexus.spring.console.jaxrs.NexusJaxRsConfiguration
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "nexus.console.web", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(ConsoleWebProperties.class)
public class ConsoleJaxRsWebConfiguration {

    /**
     * 宿主未提供 {@link TokenIssuer} 时，基于 {@link AuthConfig} 创建默认签发器。
     */
    @Bean
    @ConditionalOnMissingBean(TokenIssuer.class)
    TokenIssuer consoleTokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    /** 将 {@link com.innospots.nexus.base.exception.NexusException} 转为 legacy {@code R} 响应。 */
    @Bean
    ConsoleJaxRsExceptionSupport consoleJaxRsExceptionSupport() {
        return new ConsoleJaxRsExceptionSupport();
    }

    /** 按 {@link ConsoleWebProperties#getCors()} 输出 CORS 响应头。 */
    @Bean
    ConsoleCorsFilter consoleCorsFilter(ConsoleWebProperties webProperties) {
        return new ConsoleCorsFilter(webProperties);
    }

    /** 解析 Bearer 令牌并填充 {@link com.innospots.nexus.base.thread.SessionContext}。 */
    @Bean
    ConsoleAuthenticationFilter consoleAuthenticationFilter(
            ConsoleWebProperties webProperties,
            TokenIssuer tokenIssuer) {
        return new ConsoleAuthenticationFilter(webProperties, tokenIssuer);
    }

    /** 关闭请求侧安全时，按 {@link ConsoleWebProperties.Security#getDevSession()} 注入开发会话。 */
    @Bean
    ConsoleDevSessionFilter consoleDevSessionFilter(ConsoleWebProperties webProperties) {
        return new ConsoleDevSessionFilter(webProperties);
    }

    /** 对 {@link ConsoleWebProperties.Security#getConsolePathPatterns()} 命中路径执行页面权限校验。 */
    @Bean
    ConsolePagePermissionFilter consolePagePermissionFilter(
            ConsoleWebProperties webProperties,
            ConsolePagePermissionAuthorizer pagePermissionAuthorizer,
            AuthorizationSubjectResolver subjectResolver) {
        return new ConsolePagePermissionFilter(webProperties, pagePermissionAuthorizer, subjectResolver);
    }

    /** 分配 {@code X-Request-Id}、写入 TLC，并在响应结束后清理线程上下文。 */
    @Bean
    ConsoleRequestContextFilter consoleRequestContextFilter() {
        return new ConsoleRequestContextFilter();
    }

    /** 映射 {@link com.innospots.nexus.base.exception.NexusException}。 */
    @Bean
    ConsoleNexusExceptionMapper consoleNexusExceptionMapper(ConsoleJaxRsExceptionSupport exceptionSupport) {
        return new ConsoleNexusExceptionMapper(exceptionSupport);
    }

    /** 未捕获异常的兜底映射。 */
    @Bean
    ConsoleThrowableExceptionMapper consoleThrowableExceptionMapper(ConsoleJaxRsExceptionSupport exceptionSupport) {
        return new ConsoleThrowableExceptionMapper(exceptionSupport);
    }

    /**
     * 将上述过滤器与异常映射注册到 Jersey {@code ResourceConfig}（与业务 {@code @Path} 资源并列）。
     */
    @Bean
    ResourceConfigCustomizer consoleJaxRsWebResourceConfigCustomizer(
            ConsoleCorsFilter corsFilter,
            ConsoleAuthenticationFilter authenticationFilter,
            ConsoleDevSessionFilter devSessionFilter,
            ConsolePagePermissionFilter pagePermissionFilter,
            ConsoleRequestContextFilter requestContextFilter,
            ConsoleNexusExceptionMapper nexusExceptionMapper,
            ConsoleThrowableExceptionMapper throwableExceptionMapper) {
        return resourceConfig -> {
            resourceConfig.register(corsFilter);
            resourceConfig.register(authenticationFilter);
            resourceConfig.register(devSessionFilter);
            resourceConfig.register(pagePermissionFilter);
            resourceConfig.register(requestContextFilter);
            resourceConfig.register(nexusExceptionMapper);
            resourceConfig.register(throwableExceptionMapper);
        };
    }
}

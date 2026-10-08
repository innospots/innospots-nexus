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
import com.innospots.nexus.console.endpoint.MainRootEndpoint;
import com.innospots.nexus.console.jaxrs.filter.ConsoleAuthenticationFilter;
import com.innospots.nexus.console.jaxrs.filter.ConsoleCorsFilter;
import com.innospots.nexus.console.jaxrs.filter.ConsoleDevSessionFilter;
import com.innospots.nexus.console.jaxrs.filter.ConsolePagePermissionFilter;
import com.innospots.nexus.console.jaxrs.filter.ConsoleRequestContextFilter;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;

/**
 * 管理控制台 Jersey 横切过滤器的 Spring 装配。
 *
 * <p>在 Servlet + Jersey Filter 模式下将过滤器注册到
 * {@link org.glassfish.jersey.server.ResourceConfig}；行为由 {@link ConsoleWebProperties} 驱动。</p>
 *
 * <p>通用异常映射（{@code JaxRsExceptionSupport} 与三个 Mapper）由宿主级的
 * {@code com.innospots.nexus.spring.core.jaxrs.NexusJaxRsExceptionConfiguration} 提供，
 * 此处不再重复声明。</p>
 *
 * @author Smars
 * @date 2026/09/25
 * @see ConsoleWebProperties
 * @see com.innospots.nexus.spring.core.jaxrs.NexusJaxRsConfiguration
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

    /** 按 {@link ConsoleWebProperties#getCors()} 输出 CORS 响应头。 */
    @Bean
    ConsoleCorsFilter consoleCorsFilter(ConsoleWebProperties webProperties) {
        return new ConsoleCorsFilter(webProperties.getCors());
    }

    /** 解析 Bearer 令牌并填充 {@link com.innospots.nexus.base.thread.SessionContext}。 */
    @Bean
    ConsoleAuthenticationFilter consoleAuthenticationFilter(
            ConsoleWebProperties webProperties,
            TokenIssuer tokenIssuer) {
        return new ConsoleAuthenticationFilter(webProperties.getSecurity(), tokenIssuer);
    }

    /** 关闭请求侧安全时，按 {@link com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings#getDevSession()} 注入开发会话。 */
    @Bean
    ConsoleDevSessionFilter consoleDevSessionFilter(ConsoleWebProperties webProperties) {
        return new ConsoleDevSessionFilter(webProperties.getSecurity());
    }

    /** 对 {@link com.innospots.nexus.console.jaxrs.web.ConsoleWebSecuritySettings#getConsolePathPatterns()} 命中路径执行页面权限校验。 */
    @Bean
    ConsolePagePermissionFilter consolePagePermissionFilter(
            ConsoleWebProperties webProperties,
            ConsolePagePermissionAuthorizer pagePermissionAuthorizer,
            AuthorizationSubjectResolver subjectResolver) {
        return new ConsolePagePermissionFilter(
                webProperties.getSecurity(), pagePermissionAuthorizer, subjectResolver);
    }

    /** 分配 {@code X-Request-Id}、写入 TLC，并在响应结束后清理线程上下文。 */
    @Bean
    ConsoleRequestContextFilter consoleRequestContextFilter() {
        return new ConsoleRequestContextFilter();
    }

    /** {@code GET /} 重定向至 {@link com.innospots.nexus.console.jaxrs.web.ConsoleWebJerseySettings#getRootPath()}。 */
    @Bean
    MainRootEndpoint mainRootEndpoint(ConsoleWebProperties webProperties) {
        return new MainRootEndpoint(webProperties.getJersey().getRootPath());
    }

    /** 将上述过滤器注册到 Jersey {@code ResourceConfig}（异常映射由宿主级装配提供）。 */
    @Bean
    ResourceConfigCustomizer consoleJaxRsWebResourceConfigCustomizer(
            ConsoleCorsFilter corsFilter,
            ConsoleAuthenticationFilter authenticationFilter,
            ConsoleDevSessionFilter devSessionFilter,
            ConsolePagePermissionFilter pagePermissionFilter,
            ConsoleRequestContextFilter requestContextFilter) {
        return resourceConfig -> {
            resourceConfig.register(corsFilter);
            resourceConfig.register(authenticationFilter);
            resourceConfig.register(devSessionFilter);
            resourceConfig.register(pagePermissionFilter);
            resourceConfig.register(requestContextFilter);
        };
    }
}

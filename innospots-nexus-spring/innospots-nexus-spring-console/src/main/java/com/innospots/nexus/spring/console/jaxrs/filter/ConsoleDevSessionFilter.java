package com.innospots.nexus.spring.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.spring.console.config.ConsoleWebProperties;
import com.innospots.nexus.spring.console.jaxrs.support.ConsoleDevSessionBinder;

/**
 * 在 {@link ConsoleWebProperties.Security#isEnabled()} 为 {@code false} 时，按
 * {@link ConsoleWebProperties.Security#getDevSession()} 注入开发用 {@link SessionContext}。
 *
 * <p>与 {@link ConsoleAuthenticationFilter} 职责分离：认证 Filter 只处理 Bearer 令牌；本 Filter 仅在关闭
 * 请求侧安全时提供可配置的固定身份，便于本地调试依赖租户/工作区上下文的接口。</p>
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 10)
public final class ConsoleDevSessionFilter implements ContainerRequestFilter {

    private final ConsoleWebProperties.Security security;

    public ConsoleDevSessionFilter(ConsoleWebProperties webProperties) {
        this.security = webProperties.getSecurity();
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (security.isEnabled()) {
            return;
        }
        ConsoleWebProperties.Security.DevSession devSession = security.getDevSession();
        if (devSession == null || !devSession.isEnabled()) {
            return;
        }
        if (SessionContext.user().isPresent()) {
            return;
        }
        ConsoleDevSessionBinder.bind(devSession);
    }
}

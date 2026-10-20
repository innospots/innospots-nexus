package com.innospots.nexus.console.jaxrs.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestGrants;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestProperties;
import com.innospots.nexus.console.jaxrs.support.ResolvedEndpointBinding;
import com.innospots.nexus.console.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.logger.support.AuditLogInvocationState;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;

/**
 * 读取 {@link ConsoleResolvedEndpointGrantFilter} 写入的 {@link ResolvedEndpointBinding} 并记录审计日志。
 *
 * <p>仅当绑定存在且 {@link ResolvedEndpointGrant#auditLog()} 为 true 时落库；响应阶段优先于会话清理执行。</p>
 *
 * @author Smars
 * @date 2026/10/17
 * @see ConsoleRequestGrants
 * @see AuditLogRecorder
 */
@Provider
@Priority(Priorities.USER + 100)
public final class ConsoleAuditLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_MILLIS = ConsoleAuditLogFilter.class.getName() + ".startTimeMillis";

    private final AuditLogRecorder recorder;

    /**
     * 创建框架无关的 JAX-RS 审计过滤器。
     *
     * @param recorder 框架无关审计记录器
     */
    public ConsoleAuditLogFilter(AuditLogRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty(START_TIME_MILLIS, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        try {
            Object startTime = requestContext.getProperty(START_TIME_MILLIS);
            requestContext.removeProperty(START_TIME_MILLIS);
            ResolvedEndpointBinding binding = ConsoleRequestGrants.binding(requestContext);
            if (binding == null || !binding.grant().auditLog()) {
                return;
            }
            String path = requestContext.getUriInfo().getPath();
            AuditLogInvocationState state = new AuditLogInvocationState(
                    binding.grant(),
                    binding.resourceClassName(),
                    binding.resourceMethodName(),
                    requestContext.getMethod(),
                    path.startsWith("/") ? path : "/" + path,
                    startTime instanceof Long millis ? millis : System.currentTimeMillis());
            String actor = SessionContext.user()
                    .map(UserSnapshot::userName)
                    .filter(name -> !name.isBlank())
                    .orElse("");
            Object failure = requestContext.getProperty(RequestProperties.FAILURE_CAUSE);
            recorder.record(
                    state,
                    actor,
                    responseContext.getStatus(),
                    responseContext.getEntity(),
                    failure instanceof Throwable cause ? cause : null);
        } finally {
            requestContext.removeProperty(ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING);
        }
    }
}

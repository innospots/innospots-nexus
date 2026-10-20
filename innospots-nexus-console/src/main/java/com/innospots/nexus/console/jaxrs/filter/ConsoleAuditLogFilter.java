package com.innospots.nexus.console.jaxrs.filter;

import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.base.domain.identity.UserSnapshot;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.console.jaxrs.support.EndpointMethodResolver;
import com.innospots.nexus.console.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.console.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.logger.support.AuditLogInvocationState;
import com.innospots.nexus.console.logger.support.AuditLogRecorder;
import com.innospots.nexus.core.jaxrs.support.RequestProperties;

/**
 * 通过宿主提供的 {@link EndpointMethodResolver} 定位资源方法并记录审计日志。
 *
 * <p>仅记录带审计注解的方法，响应优先于会话清理执行。</p>
 *
 * @author Smars
 * @date 2026/10/17
 * @see EndpointGrantAnnotations
 * @see AuditLogRecorder
 */
@Provider
@Priority(Priorities.USER + 100)
public final class ConsoleAuditLogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String START_TIME_MILLIS = ConsoleAuditLogFilter.class.getName() + ".startTimeMillis";

    private final AuditLogRecorder recorder;
    private final EndpointMethodResolver endpointMethodResolver;

    /**
     * 创建框架无关的 JAX-RS 审计过滤器。
     *
     * @param recorder 框架无关审计记录器
     * @param endpointMethodResolver 宿主提供的资源方法定位器
     */
    public ConsoleAuditLogFilter(AuditLogRecorder recorder, EndpointMethodResolver endpointMethodResolver) {
        this.recorder = recorder;
        this.endpointMethodResolver = endpointMethodResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        requestContext.setProperty(START_TIME_MILLIS, System.currentTimeMillis());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object startTime = requestContext.getProperty(START_TIME_MILLIS);
        requestContext.removeProperty(START_TIME_MILLIS);
        ResourceInfo resourceInfo = endpointMethodResolver.resolve(requestContext);
        if (resourceInfo == null) {
            return;
        }
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, resourceClass);
        if (grant == null || !grant.auditLog()) {
            return;
        }
        String path = requestContext.getUriInfo().getPath();
        AuditLogInvocationState state = new AuditLogInvocationState(
                grant,
                resourceClass.getName(),
                method.getName(),
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
    }
}

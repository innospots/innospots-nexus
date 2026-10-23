package com.innospots.nexus.console.jaxrs.filter;

import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.ext.Provider;

import com.innospots.nexus.core.endpoint.grant.support.EndpointGrantAnnotations;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;
import com.innospots.nexus.console.jaxrs.support.ConsoleRequestProperties;
import com.innospots.nexus.console.jaxrs.support.EndpointMethodResolver;
import com.innospots.nexus.console.jaxrs.support.ResolvedEndpointBinding;

/**
 * 在认证与页面权限过滤器之前解析 {@link ResolvedEndpointGrant} 并写入请求上下文。
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public final class ConsoleResolvedEndpointGrantFilter implements ContainerRequestFilter {

    private final EndpointMethodResolver endpointMethodResolver;

    public ConsoleResolvedEndpointGrantFilter(EndpointMethodResolver endpointMethodResolver) {
        this.endpointMethodResolver = endpointMethodResolver;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ResourceInfo resourceInfo = endpointMethodResolver.resolve(requestContext);
        if (resourceInfo == null) {
            return;
        }
        Method method = resourceInfo.getResourceMethod();
        Class<?> resourceClass = resourceInfo.getResourceClass();
        if (method == null || resourceClass == null) {
            return;
        }
        ResolvedEndpointGrant grant = EndpointGrantAnnotations.resolve(method, resourceClass);
        if (grant != null) {
            requestContext.setProperty(
                    ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING,
                    new ResolvedEndpointBinding(grant, resourceClass.getName(), method.getName()));
        }
    }
}

package com.innospots.nexus.console.jaxrs.support;

import jakarta.ws.rs.container.ContainerRequestContext;

import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;

/**
 * 从 JAX-RS 请求上下文读取 {@link ResolvedEndpointGrant} 与关联绑定。
 */
public final class ConsoleRequestGrants {

    private ConsoleRequestGrants() {
    }

    /**
     * 返回由 {@link com.innospots.nexus.console.jaxrs.filter.ConsoleResolvedEndpointGrantFilter}
     * 写入的绑定；未解析或未声明时为 null。
     */
    public static ResolvedEndpointBinding binding(ContainerRequestContext requestContext) {
        Object value = requestContext.getProperty(ConsoleRequestProperties.RESOLVED_ENDPOINT_BINDING);
        if (value instanceof ResolvedEndpointBinding endpointBinding) {
            return endpointBinding;
        }
        return null;
    }

    /**
     * 返回绑定中的合并策略；未解析或未声明时为 null。
     */
    public static ResolvedEndpointGrant resolvedGrant(ContainerRequestContext requestContext) {
        ResolvedEndpointBinding endpointBinding = binding(requestContext);
        if (endpointBinding == null) {
            return null;
        }
        return endpointBinding.grant();
    }
}

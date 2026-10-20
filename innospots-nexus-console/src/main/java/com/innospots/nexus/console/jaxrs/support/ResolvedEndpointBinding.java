package com.innospots.nexus.console.jaxrs.support;

import com.innospots.nexus.console.endpoint.grant.support.ResolvedEndpointGrant;

/**
 * {@link com.innospots.nexus.console.jaxrs.filter.ConsoleResolvedEndpointGrantFilter}
 * 写入请求上下文的 endpoint 解析结果。
 */
public record ResolvedEndpointBinding(
        ResolvedEndpointGrant grant,
        String resourceClassName,
        String resourceMethodName
) {

    public ResolvedEndpointBinding {
        resourceClassName = resourceClassName == null ? "" : resourceClassName;
        resourceMethodName = resourceMethodName == null ? "" : resourceMethodName;
    }
}

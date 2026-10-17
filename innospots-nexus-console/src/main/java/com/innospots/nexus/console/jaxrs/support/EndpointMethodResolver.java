package com.innospots.nexus.console.jaxrs.support;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

/**
 * 由宿主定位当前请求匹配的 endpoint 方法与资源实现类。
 *
 * @author Smars
 * @date 2026/10/17
 * @see ResourceInfo
 * @see com.innospots.nexus.console.jaxrs.filter.ConsoleAuditLogFilter
 */
@FunctionalInterface
public interface EndpointMethodResolver {

    /**
     * 在请求匹配完成后定位资源方法。
     *
     * @param requestContext 当前 JAX-RS 请求上下文
     * @return 包含方法与实现类的资源信息；未匹配资源时为 null
     */
    ResourceInfo resolve(ContainerRequestContext requestContext);
}

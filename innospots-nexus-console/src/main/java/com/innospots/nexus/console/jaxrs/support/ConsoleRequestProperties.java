package com.innospots.nexus.console.jaxrs.support;

/**
 * 管理控制台 JAX-RS 请求属性键。
 */
public final class ConsoleRequestProperties {

    /**
     * 当前请求匹配资源方法后的 grant 与审计定位信息。
     */
    public static final String RESOLVED_ENDPOINT_BINDING =
            ConsoleRequestProperties.class.getName() + ".resolvedEndpointBinding";

    private ConsoleRequestProperties() {
    }
}

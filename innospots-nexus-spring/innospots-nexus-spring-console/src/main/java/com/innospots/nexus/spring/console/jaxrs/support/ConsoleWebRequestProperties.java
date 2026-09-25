package com.innospots.nexus.spring.console.jaxrs.support;

/**
 * JAX-RS 请求属性键。
 */
public final class ConsoleWebRequestProperties {

    public static final String REQUEST_ID = ConsoleWebRequestProperties.class.getName() + ".requestId";

    public static final String AUTHORIZATION_CONTEXT =
            ConsoleWebRequestProperties.class.getName() + ".authorizationContext";

    private ConsoleWebRequestProperties() {
    }
}

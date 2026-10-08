package com.innospots.nexus.core.jaxrs.support;

/**
 * Jakarta REST 请求属性键。
 */
public final class RequestProperties {

    public static final String REQUEST_ID = RequestProperties.class.getName() + ".requestId";

    public static final String AUTHORIZATION_CONTEXT =
            RequestProperties.class.getName() + ".authorizationContext";

    private RequestProperties() {
    }
}

package com.innospots.nexus.core.jaxrs.support;

/**
 * Jakarta REST 请求属性键。
 */
public final class RequestProperties {

    public static final String REQUEST_ID = RequestProperties.class.getName() + ".requestId";

    public static final String AUTHORIZATION_CONTEXT =
            RequestProperties.class.getName() + ".authorizationContext";

    /**
     * 请求处理期间捕获的失败原因，供审计等横切能力读取。
     */
    public static final String FAILURE_CAUSE = RequestProperties.class.getName() + ".failureCause";

    private RequestProperties() {
    }
}

package com.innospots.nexus.quarkus.service.rest;

/**
 * 单次 REST 请求生命周期对象。
 */
public final class ServiceRequestLifecycle {

    public static final String REQUEST_PROPERTY = ServiceRequestLifecycle.class.getName();
    public static final String STARTED_AT_PROPERTY = ServiceRequestLifecycle.class.getName() + ".startedAt";

    private final String requestId;

    private final com.innospots.nexus.service.runtime.cancellation.CancellationSource cancellationSource;

    /**
     * 创建请求生命周期。
     *
     * @param requestId          请求标识
     * @param cancellationSource 取消源
     */
    public ServiceRequestLifecycle(
            String requestId,
            com.innospots.nexus.service.runtime.cancellation.CancellationSource cancellationSource) {
        this.requestId = requestId;
        this.cancellationSource = cancellationSource;
    }

    public String requestId() {
        return requestId;
    }

    public com.innospots.nexus.service.runtime.cancellation.CancellationSource cancellationSource() {
        return cancellationSource;
    }
}

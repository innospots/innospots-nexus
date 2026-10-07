package com.innospots.nexus.spring.service.http.mvc;

import com.innospots.nexus.service.runtime.cancellation.CancellationSource;

/**
 * 单次 HTTP 请求生命周期对象。
 */
public final class ServiceRequestLifecycle {

    public static final String REQUEST_ATTRIBUTE = ServiceRequestLifecycle.class.getName();

    private final String requestId;
    private final CancellationSource cancellationSource;

    /**
     * 创建请求生命周期。
     *
     * @param requestId           请求标识
     * @param cancellationSource  取消源
     */
    public ServiceRequestLifecycle(String requestId, CancellationSource cancellationSource) {
        this.requestId = requestId;
        this.cancellationSource = cancellationSource;
    }

    public String requestId() {
        return requestId;
    }

    public CancellationSource cancellationSource() {
        return cancellationSource;
    }
}

package com.innospots.nexus.sample.spring.platform.role.support;

import java.net.http.HttpResponse;
import java.util.Map;

import com.innospots.nexus.sample.spring.platform.support.PlatformEndpointHttpClient;

/**
 * 角色 REST HTTP 集成测试客户端（委托 {@link PlatformEndpointHttpClient}）。
 */
public final class RoleEndpointHttpClient {

    private final PlatformEndpointHttpClient delegate;

    public RoleEndpointHttpClient(int port) {
        this.delegate = new PlatformEndpointHttpClient(port);
    }

    public HttpResponse<String> get(String path) throws Exception {
        return delegate.get(path);
    }

    public HttpResponse<String> postJson(String path, String jsonBody) throws Exception {
        return delegate.postJson(path, jsonBody);
    }

    public HttpResponse<String> exchange(
            String method,
            String path,
            Map<String, String> headers,
            String body) throws Exception {
        return delegate.exchange(method, path, headers, body);
    }
}

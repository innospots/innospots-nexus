package com.innospots.nexus.service.adapter.test.support;

import java.net.URI;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;

/**
 * 黑盒场景目标宿主描述。
 *
 * @param baseUrl        宿主根 URL
 * @param defaultHeaders 默认请求头
 * @author Smars
 * @date 2026/09/15
 */
public record AdapterTestTarget(URI baseUrl, Map<String, String> defaultHeaders) {

    public AdapterTestTarget {
        Checks.notNull(baseUrl, "baseUrl");
        defaultHeaders = defaultHeaders == null ? Map.of() : Map.copyOf(defaultHeaders);
    }

    /**
     * 解析相对路径。
     *
     * @param path 相对路径
     * @return 绝对 URI
     */
    public URI resolve(String path) {
        Checks.notBlank(path, "path");
        String normalized = path.startsWith("/") ? path : "/" + path;
        return baseUrl.resolve(normalized);
    }
}

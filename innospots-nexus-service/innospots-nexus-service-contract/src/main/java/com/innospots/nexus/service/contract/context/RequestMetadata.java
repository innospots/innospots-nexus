package com.innospots.nexus.service.contract.context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;

/**
 * 传输层请求元数据。请求头名称小写化，值被复制。
 *
 * @param method        HTTP 或协议方法
 * @param path          请求路径
 * @param routeTemplate 匹配的路由模板，匹配前为空
 * @param headers       允许的小写名称请求头
 * @param remoteAddress 调用方地址
 * @param clientId      可选客户端标识
 * @author Smars
 * @date 2026/09/13
 * @see ServiceContext
 */
public record RequestMetadata(
        String method,
        String path,
        String routeTemplate,
        Map<String, List<String>> headers,
        String remoteAddress,
        String clientId
) {

    public RequestMetadata {
        Checks.notBlank(method, "method");
        Checks.notBlank(path, "path");
        Checks.notBlank(remoteAddress, "remoteAddress");
        routeTemplate = blankToNull(routeTemplate);
        clientId = blankToNull(clientId);
        headers = copyHeaders(headers);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private static Map<String, List<String>> copyHeaders(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> copied = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            Checks.notBlank(entry.getKey(), "header name");
            String name = entry.getKey().toLowerCase(Locale.ROOT);
            if (copied.containsKey(name)) {
                throw NexusException.build(NexusStatusCode.INVALID_PARAMETER, "duplicate header name");
            }
            List<String> values = entry.getValue() == null ? List.of() : List.copyOf(entry.getValue());
            copied.put(name, values);
        }
        return Map.copyOf(copied);
    }
}

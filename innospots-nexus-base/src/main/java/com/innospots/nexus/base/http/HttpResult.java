package com.innospots.nexus.base.http;

import java.util.List;
import java.util.Map;

/**
 * HTTP 请求结果的不可变记录。包含状态码、原因短语、响应体与响应头。
 *
 * @author Smars
 * @date 2026/09/13
 * @param statusCode    HTTP 状态码
 * @param reasonPhrase  原因短语
 * @param body          响应体
 * @param headers       响应头（多值）
 * @see HttpUtils
 */
public record HttpResult(
        int statusCode,
        String reasonPhrase,
        String body,
        Map<String, List<String>> headers
) {

    /**
     * 判断响应是否成功（状态码 2xx）。
     *
     * @return 成功时返回 {@code true}
     */
    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}

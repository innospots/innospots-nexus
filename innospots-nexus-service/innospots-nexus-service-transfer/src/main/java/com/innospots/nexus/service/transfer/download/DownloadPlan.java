package com.innospots.nexus.service.transfer.download;

import java.util.Map;

import com.innospots.nexus.service.transfer.content.ByteRange;

/**
 * 下载执行计划。
 *
 * @param httpStatus     HTTP 状态码
 * @param contentLength  响应 Content-Length，未知时为 {@code null}
 * @param contentRange   响应 Content-Range
 * @param headers        附加响应头
 * @param readRange      实际读取范围，无 body 时为 {@code null}
 * @param bodyRequired   是否需要写出 body
 */
public record DownloadPlan(
        int httpStatus,
        Long contentLength,
        String contentRange,
        Map<String, String> headers,
        ByteRange readRange,
        boolean bodyRequired
) {

    public DownloadPlan {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }
}

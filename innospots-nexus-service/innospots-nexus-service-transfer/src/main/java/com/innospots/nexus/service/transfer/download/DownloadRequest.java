package com.innospots.nexus.service.transfer.download;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.ByteRange;

/**
 * 条件下载请求。
 *
 * @param method              HTTP 方法
 * @param ifMatch             If-Match
 * @param ifNoneMatch         If-None-Match
 * @param ifModifiedSince     If-Modified-Since
 * @param ifUnmodifiedSince   If-Unmodified-Since
 * @param ifRange             If-Range
 * @param range               解析后的 Range
 */
public record DownloadRequest(
        String method,
        String ifMatch,
        String ifNoneMatch,
        Instant ifModifiedSince,
        Instant ifUnmodifiedSince,
        String ifRange,
        ByteRange range
) {

    public DownloadRequest {
        Checks.notBlank(method, "method");
        ifMatch = blankToNull(ifMatch);
        ifNoneMatch = blankToNull(ifNoneMatch);
        ifRange = blankToNull(ifRange);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}

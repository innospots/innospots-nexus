package com.innospots.nexus.service.transfer.download;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.ByteRange;

/**
 * 从 HTTP 头构造 {@link DownloadRequest}。
 */
public final class DownloadRequests {

    private DownloadRequests() {
    }

    /**
     * 从方法与请求头构造下载请求。
     *
     * @param method  HTTP 方法
     * @param headers 小写化请求头
     * @return 下载请求
     */
    public static DownloadRequest from(String method, Map<String, List<String>> headers) {
        Checks.notBlank(method, "method");
        Map<String, List<String>> safeHeaders = headers == null ? Map.of() : headers;
        return new DownloadRequest(
                method,
                firstHeader(safeHeaders, "if-match"),
                firstHeader(safeHeaders, "if-none-match"),
                parseInstant(firstHeader(safeHeaders, "if-modified-since")),
                parseInstant(firstHeader(safeHeaders, "if-unmodified-since")),
                firstHeader(safeHeaders, "if-range"),
                parseRange(firstHeader(safeHeaders, "range")));
    }

    private static String firstHeader(Map<String, List<String>> headers, String name) {
        List<String> values = headers.get(name.toLowerCase(Locale.ROOT));
        if (values == null || values.isEmpty()) {
            return null;
        }
        String value = values.getFirst();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Instant parseInstant(String value) {
        if (value == null) {
            return null;
        }
        return Instant.parse(value);
    }

    private static ByteRange parseRange(String value) {
        if (value == null || !value.toLowerCase(Locale.ROOT).startsWith("bytes=")) {
            return null;
        }
        String spec = value.substring("bytes=".length()).trim();
        int dash = spec.indexOf('-');
        if (dash < 0) {
            return null;
        }
        String startText = spec.substring(0, dash).trim();
        String endText = spec.substring(dash + 1).trim();
        if (startText.isEmpty()) {
            long suffix = Long.parseLong(endText);
            return new ByteRange(0, suffix - 1);
        }
        long start = Long.parseLong(startText);
        if (endText.isEmpty()) {
            return new ByteRange(start, Long.MAX_VALUE);
        }
        long end = Long.parseLong(endText);
        return new ByteRange(start, end);
    }
}

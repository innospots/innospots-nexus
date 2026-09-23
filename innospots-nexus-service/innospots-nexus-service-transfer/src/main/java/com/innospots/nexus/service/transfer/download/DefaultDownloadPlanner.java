package com.innospots.nexus.service.transfer.download;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.transfer.content.ByteRange;
import com.innospots.nexus.service.transfer.content.ContentMetadata;

/**
 * 基于 RFC 9110 子集的默认下载规划器。
 */
public final class DefaultDownloadPlanner implements DownloadPlanner {

    @Override
    public DownloadPlan plan(DownloadRequest request, ContentMetadata metadata) {
        Checks.notNull(request, "request");
        Checks.notNull(metadata, "metadata");
        if (request.ifMatch() != null && !etagMatches(request.ifMatch(), metadata.etag())) {
            return preconditionFailed(metadata);
        }
        if (request.ifUnmodifiedSince() != null
                && metadata.lastModified() != null
                && metadata.lastModified().isAfter(request.ifUnmodifiedSince())) {
            return preconditionFailed(metadata);
        }
        if (matchesIfNoneMatch(request, metadata) || matchesIfModifiedSince(request, metadata)) {
            return notModified(metadata);
        }
        if ("HEAD".equalsIgnoreCase(request.method())) {
            return fullResponse(metadata, false);
        }
        if (request.range() == null || !metadata.rangeSupported()) {
            return fullResponse(metadata, true);
        }
        ByteRange requested = request.range();
        if (requested.endInclusive() >= metadata.size()) {
            return rangeNotSatisfiable(metadata);
        }
        if (request.ifRange() != null && !etagMatches(request.ifRange(), metadata.etag())) {
            return fullResponse(metadata, true);
        }
        return partialContent(metadata, requested);
    }

    private static DownloadPlan preconditionFailed(ContentMetadata metadata) {
        return response(412, metadata, null, null, false);
    }

    private static DownloadPlan notModified(ContentMetadata metadata) {
        return response(304, metadata, null, null, false);
    }

    private static DownloadPlan rangeNotSatisfiable(ContentMetadata metadata) {
        Map<String, String> headers = baseHeaders(metadata);
        headers.put("Content-Range", "bytes */" + metadata.size());
        return new DownloadPlan(416, 0L, headers.get("Content-Range"), headers, null, false);
    }

    private static DownloadPlan fullResponse(ContentMetadata metadata, boolean bodyRequired) {
        ByteRange readRange = bodyRequired ? new ByteRange(0, metadata.size() - 1) : null;
        Long length = bodyRequired ? metadata.size() : null;
        return response(200, metadata, readRange, length, bodyRequired);
    }

    private static DownloadPlan partialContent(ContentMetadata metadata, ByteRange range) {
        Map<String, String> headers = baseHeaders(metadata);
        String contentRange = "bytes " + range.startInclusive() + "-" + range.endInclusive() + "/" + metadata.size();
        headers.put("Content-Range", contentRange);
        return new DownloadPlan(
                206,
                range.length(),
                contentRange,
                headers,
                range,
                true);
    }

    private static DownloadPlan response(
            int status,
            ContentMetadata metadata,
            ByteRange readRange,
            Long contentLength,
            boolean bodyRequired) {
        Map<String, String> headers = baseHeaders(metadata);
        if (metadata.rangeSupported()) {
            headers.put("Accept-Ranges", "bytes");
        }
        if (contentLength != null) {
            headers.put("Content-Length", Long.toString(contentLength));
        }
        return new DownloadPlan(status, contentLength, headers.get("Content-Range"), headers, readRange, bodyRequired);
    }

    private static Map<String, String> baseHeaders(ContentMetadata metadata) {
        Map<String, String> headers = new LinkedHashMap<>();
        if (metadata.contentType() != null) {
            headers.put("Content-Type", metadata.contentType());
        }
        if (metadata.etag() != null) {
            headers.put("ETag", metadata.etag());
        }
        if (metadata.lastModified() != null) {
            headers.put("Last-Modified", metadata.lastModified().toString());
        }
        return headers;
    }

    private static boolean matchesIfNoneMatch(DownloadRequest request, ContentMetadata metadata) {
        if (request.ifNoneMatch() == null || metadata.etag() == null) {
            return false;
        }
        return etagMatches(request.ifNoneMatch(), metadata.etag());
    }

    private static boolean matchesIfModifiedSince(DownloadRequest request, ContentMetadata metadata) {
        if (request.ifModifiedSince() == null || metadata.lastModified() == null) {
            return false;
        }
        return !metadata.lastModified().isAfter(request.ifModifiedSince());
    }

    private static boolean etagMatches(String headerValue, String etag) {
        if (headerValue == null || etag == null) {
            return false;
        }
        if ("*".equals(headerValue.trim())) {
            return true;
        }
        String[] tokens = headerValue.split(",");
        for (String token : tokens) {
            if (Objects.equals(token.trim(), etag)) {
                return true;
            }
        }
        return false;
    }
}

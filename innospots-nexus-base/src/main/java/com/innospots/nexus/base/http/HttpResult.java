package com.innospots.nexus.base.http;

import java.util.List;
import java.util.Map;

/**
 * Immutable result of an HTTP request. Contains the status code, reason
 * phrase, response body, and response headers.
 */
public record HttpResult(
        int statusCode,
        String reasonPhrase,
        String body,
        Map<String, List<String>> headers
) {

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}

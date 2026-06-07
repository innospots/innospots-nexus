package com.innospots.nexus.base.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ApiRequest {

    private String uri;
    private String method;
    private Map<String, Object> params = new LinkedHashMap<>();
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Map<String, Object> body = new LinkedHashMap<>();

    public ApiRequest() {
    }

    public static ApiRequest of(String method, String uri) {
        ApiRequest request = new ApiRequest();
        request.method = method;
        request.uri = uri;
        return request;
    }

    public static ApiRequest get(String uri) {
        return of("GET", uri);
    }

    public static ApiRequest post(String uri) {
        return of("POST", uri);
    }

    public static ApiRequest put(String uri) {
        return of("PUT", uri);
    }

    public static ApiRequest delete(String uri) {
        return of("DELETE", uri);
    }

    public String uri() {
        return uri;
    }

    public String method() {
        return method;
    }

    public Map<String, Object> params() {
        return Map.copyOf(params);
    }

    public ApiRequest param(String key, Object value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    public Map<String, Object> headers() {
        return Map.copyOf(headers);
    }

    public ApiRequest header(String key, Object value) {
        if (key != null) {
            headers.put(key, value);
        }
        return this;
    }

    public Map<String, Object> body() {
        return Map.copyOf(body);
    }

    public ApiRequest body(String key, Object value) {
        if (key != null) {
            body.put(key, value);
        }
        return this;
    }
}

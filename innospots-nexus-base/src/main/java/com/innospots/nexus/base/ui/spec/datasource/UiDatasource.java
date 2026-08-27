package com.innospots.nexus.base.ui.spec.datasource;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Getter;
import lombok.Setter;

import com.innospots.nexus.base.ui.spec.ApiRequest;

/**
 * Page datasource specification containing its server URL and request
 * templates. Authorization assignments are deliberately excluded.
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class UiDatasource {

    private String method;
    private String url;
    private Map<String, Object> params = new LinkedHashMap<>();
    private Map<String, Object> headers = new LinkedHashMap<>();
    private Map<String, Object> body = new LinkedHashMap<>();

    /** Creates an empty datasource for deserialization. */
    public UiDatasource() {
    }

    /** Creates a GET datasource. */
    public static UiDatasource get(String url) {
        return of("GET", url);
    }

    /** Creates a POST datasource. */
    public static UiDatasource post(String url) {
        return of("POST", url);
    }

    /** Creates a datasource for the supplied HTTP method and URL. */
    public static UiDatasource of(String method, String url) {
        UiDatasource datasource = new UiDatasource();
        datasource.method = method;
        datasource.url = url;
        return datasource;
    }

    /** Converts a simple API request into a datasource declaration. */
    public static UiDatasource from(ApiRequest request) {
        UiDatasource datasource = of(request.method(), request.uri());
        datasource.params.putAll(request.params());
        datasource.headers.putAll(request.headers());
        datasource.body.putAll(request.body());
        return datasource;
    }

    /** Returns immutable default query parameters. */
    public Map<String, Object> params() {
        return Map.copyOf(params);
    }

    /** Adds a default query parameter or template value. */
    public UiDatasource param(String key, Object value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /** Returns immutable default request headers. */
    public Map<String, Object> headers() {
        return Map.copyOf(headers);
    }

    /** Adds a default request header or template value. */
    public UiDatasource header(String key, Object value) {
        if (key != null) {
            headers.put(key, value);
        }
        return this;
    }

    /** Returns an immutable default request body. */
    public Map<String, Object> body() {
        return Map.copyOf(body);
    }

    /** Adds a default request-body field or template value. */
    public UiDatasource body(String key, Object value) {
        if (key != null) {
            body.put(key, value);
        }
        return this;
    }
}

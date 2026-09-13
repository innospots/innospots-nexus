package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP request definition used by HTTP data sources and dynamic DSL sources.
 *
 * <p>URLs and parameter values may contain Pactor expressions such as
 * {@code /api/customers/${state.selectedId}}.</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HttpRequest {

    /** HTTP method. Defaults to {@code GET} in the schema when omitted. */
    private String method = "GET";

    /** Request URL or path. */
    private String url;

    /** Query parameters. */
    private Map<String, Object> params = new LinkedHashMap<>();

    /** Request headers. */
    private Map<String, Object> headers = new LinkedHashMap<>();

    /** Optional request body for POST, PUT, and PATCH requests. */
    private Object body;

    /** Optional request timeout in milliseconds. */
    private Integer timeout;
}

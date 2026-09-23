package com.innospots.nexus.core.plugin.contribution.console.ui.spec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * HTTP 数据源与动态 DSL 源使用的 HTTP 请求定义。
 *
 * <p>URL 与参数值可包含 Pactor 表达式，例如
 * {@code /api/customers/${state.selectedId}}。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class HttpRequest {

    /** HTTP 方法；省略时 schema 默认为 {@code GET}。 */
    private String method = "GET";

    /** 请求 URL 或路径。 */
    private String url;

    /** 查询参数。 */
    private Map<String, Object> params = new LinkedHashMap<>();

    /** 请求头。 */
    private Map<String, Object> headers = new LinkedHashMap<>();

    /** POST、PUT、PATCH 请求的可选请求体。 */
    private Object body;

    /** 可选请求超时时间（毫秒）。 */
    private Integer timeout;
}

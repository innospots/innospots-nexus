package com.innospots.nexus.console.jaxrs.web;

import com.innospots.nexus.core.openapi.OpenApiCatalogPaths;

import lombok.Getter;
import lombok.Setter;

/**
 * Jersey Servlet Filter 模式下的路由行为（与 {@code spring.jersey.type=filter} 配合）。
 */
@Getter
@Setter
public class ConsoleWebJerseySettings {

    /**
     * {@code GET /} 重定向目标路径（{@link com.innospots.nexus.console.endpoint.MainRootEndpoint}）。
     *
     * <p>绑定 {@code nexus.console.web.jersey.root-path}；默认 Scalar 文档页。</p>
     */
    private String rootPath = OpenApiCatalogPaths.UI_DEFAULT;

    /**
     * 未匹配 JAX-RS 资源时是否将请求继续交给后续 Servlet 链（含 Spring MVC）。
     *
     * <p>默认 {@code false}：由 Jersey 直接响应 404，避免落入 Spring Whitelabel HTML。
     * 设为 {@code true} 时，静态资源或未注册的 Spring MVC 端点仍可被后续处理器接管。</p>
     */
    private boolean forwardOn404 = true;
}

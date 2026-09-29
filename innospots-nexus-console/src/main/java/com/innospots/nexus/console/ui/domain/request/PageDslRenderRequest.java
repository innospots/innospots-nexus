package com.innospots.nexus.console.ui.domain.request;

import jakarta.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

/**
 * 页面 DSL 渲染的 URL 路径参数绑定（与 {@code @BeanParam} 配合使用）。
 *
 * <p>运行时写入页面 {@code state} 的键值通过 <strong>GET 查询串</strong>传入（扁平 {@code ?key=value}），
 * 由 {@link com.innospots.nexus.console.ui.endpoint.DefaultPageDslEndpoint} 从 {@link jakarta.ws.rs.core.UriInfo}
 * 收集，不放在本类型中。</p>
 */
@Schema(name = "PageDslRenderRequest", description = "页面 DSL 渲染路径参数（domain / module / page）")
public final class PageDslRenderRequest {

    @Parameter(description = "项目领域键（classpath 路径段，如 demo、sales）", required = true)
    @Schema(description = "项目领域键", required = true, examples = {"demo"})
    @PathParam("domainKey")
    private String domainKey;

    @Parameter(description = "模块键，与 ui-pages 目录层级一致", required = true)
    @Schema(description = "模块键", required = true, examples = {"demo"})
    @PathParam("moduleKey")
    private String moduleKey;

    @Parameter(description = "页面键，与 Page DSL 中 page.id 一致", required = true)
    @Schema(description = "页面键", required = true, examples = {"customer-list"})
    @PathParam("pageKey")
    private String pageKey;

    public PageDslRenderRequest() {
    }

    public String domainKey() {
        return domainKey;
    }

    public String moduleKey() {
        return moduleKey;
    }

    public String pageKey() {
        return pageKey;
    }
}

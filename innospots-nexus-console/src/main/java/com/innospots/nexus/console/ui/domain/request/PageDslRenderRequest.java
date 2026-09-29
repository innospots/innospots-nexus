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
@Schema(
        name = "PageDslRenderRequest",
        description = "页面 DSL 复合 pageKey（{domainKey}-{moduleKey}-{xxx}）")
public final class PageDslRenderRequest {

    @Parameter(
            description = "复合 pageKey：{domainKey}-{moduleKey}-{xxx}（domain/module 不含连字符）",
            required = true,
            example = "nexus-role-main")
    @Schema(description = "复合 pageKey", required = true, examples = {"nexus-menu-main"})
    @PathParam("pageKey")
    private String pageKey;

    public PageDslRenderRequest() {
    }

    public String pageKey() {
        return pageKey;
    }
}

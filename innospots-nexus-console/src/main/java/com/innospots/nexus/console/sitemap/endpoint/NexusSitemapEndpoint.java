package com.innospots.nexus.console.sitemap.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.entry.ConsoleModuleDescriptor;
import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;
import com.innospots.nexus.console.sitemap.service.SitemapService;

/**
 * 内置 {@code nexus} 领域 sitemap：从 {@code ui-pages/nexus/sitemap.yaml} 加载并渲染。
 *
 * <p>当前返回 YAML 全量结构；后续按登录用户与权限裁剪 pages / menus。</p>
 */
@Path(ConsoleConstant.PUBLIC_API_PREFIX + "/sitemap/" + ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "UiSitemap", description = "动态页面 Sitemap 加载与渲染")
public final class NexusSitemapEndpoint implements SitemapEndpoint {

    private final SitemapService sitemapService;

    public NexusSitemapEndpoint(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    @GET
    @Operation(
            operationId = "uiSitemapRenderNexus",
            summary = "加载并渲染内置 nexus Sitemap",
            description = "固定加载 ui-pages/nexus/sitemap.yaml；当前返回配置全量。")
    public R<SitemapResource> renderHttp() {
        return R.ok(render());
    }

    @Override
    public SitemapResource render() {
        return sitemapService.render(ConsoleModuleDescriptor.BUILTIN_DOMAIN_KEY);
    }
}

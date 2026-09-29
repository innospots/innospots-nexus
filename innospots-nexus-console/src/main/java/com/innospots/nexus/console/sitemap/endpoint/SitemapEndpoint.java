package com.innospots.nexus.console.sitemap.endpoint;

import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;

/**
 * Sitemap 渲染端口（非 JAX-RS）。
 *
 * <p>HTTP 由 {@link NexusSitemapEndpoint} 暴露。</p>
 */
public interface SitemapEndpoint {

    /**
     * 渲染内置 nexus 领域 sitemap。
     *
     * @return sitemap 资源
     */
    SitemapResource render();
}

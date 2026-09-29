package com.innospots.nexus.console.sitemap.service;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;

/**
 * 加载并渲染 sitemap（当前按固定 YAML 全量返回，后续按会话与权限裁剪）。
 */
public final class SitemapService {

    private final SitemapConfigLoader configLoader;
    private final SitemapMapper mapper;

    public SitemapService(SitemapConfigLoader configLoader, SitemapMapper mapper) {
        if (configLoader == null || mapper == null) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Sitemap configLoader and mapper are required");
        }
        this.configLoader = configLoader;
        this.mapper = mapper;
    }

    /**
     * 渲染指定领域的 sitemap 资源。
     *
     * @param domainKey {@code ui-pages} 领域键
     * @return 对外 sitemap
     */
    public SitemapResource render(String domainKey) {
        if (domainKey == null || domainKey.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Sitemap domainKey is required");
        }
        SitemapConfig config = configLoader.load(domainKey);
        return mapper.toResource(config);
    }
}

package com.innospots.nexus.console.sitemap.service;

import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.loader.SitemapConfigLoader;

/**
 * 加载 classpath meta-sitemap（不做权限裁剪；裁剪在 navigation 模块）。
 */
public final class SitemapService {

    private final SitemapConfigLoader configLoader;

    public SitemapService(SitemapConfigLoader configLoader) {
        if (configLoader == null) {
            throw ConsoleExceptions.build(
                    NexusStatusCode.CONFIG_ERROR.fullCode(),
                    "Meta-sitemap configLoader is required");
        }
        this.configLoader = configLoader;
    }

    /**
     * @return 解析后的 meta-sitemap 文档
     */
    public SitemapConfig load() {
        return configLoader.load();
    }
}

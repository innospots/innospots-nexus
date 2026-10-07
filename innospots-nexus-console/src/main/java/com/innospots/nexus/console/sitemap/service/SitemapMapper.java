package com.innospots.nexus.console.sitemap.service;

import com.innospots.nexus.console.sitemap.domain.config.SitemapConfig;
import com.innospots.nexus.console.sitemap.domain.vo.SitemapResource;

/**
 * 将 classpath 配置映射为对外 sitemap 资源。
 */
public final class SitemapMapper {

    /**
     * 将会话无关配置复制为资源形态（暂不裁剪、不注入会话字段）。
     *
     * @param config YAML 配置
     * @return 对外资源
     */
    public SitemapResource toResource(SitemapConfig config) {
        if (config == null) {
            return null;
        }
        SitemapResource resource = new SitemapResource();
        resource.setId(config.getId());
        resource.setResourceType(config.getResourceType());
        resource.setVersion(config.getVersion());
        resource.setDslVersion(config.getDslVersion());
        resource.setApp(config.getApp());
        resource.setLayout(config.getLayout());
        resource.setAuth(config.getAuth());
        resource.setPages(config.getPages());
        resource.setMenus(config.getMenus());
        resource.setLayouts(config.getLayouts());
        return resource;
    }
}

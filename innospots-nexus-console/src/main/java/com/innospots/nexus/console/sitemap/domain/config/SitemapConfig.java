package com.innospots.nexus.console.sitemap.domain.config;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

import lombok.Getter;
import lombok.Setter;

/**
 * 从 classpath {@code meta-sitemap.yaml} 解析的默认菜单与站点元数据（会话无关）。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class SitemapConfig {

    private String id;
    private String title;
    private String description;
    private Integer version;
    private String updatedAt;
    private List<SitemapMenuItem> menus = new ArrayList<>();
}

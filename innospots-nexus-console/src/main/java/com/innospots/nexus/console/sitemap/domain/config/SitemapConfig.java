package com.innospots.nexus.console.sitemap.domain.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.console.sitemap.domain.model.SitemapAppInfo;
import com.innospots.nexus.console.sitemap.domain.model.SitemapAuthConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapLayoutDefinition;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;
import com.innospots.nexus.console.sitemap.domain.model.SitemapPageDescriptor;

import lombok.Getter;
import lombok.Setter;

/**
 * 从 classpath YAML 解析的 sitemap 配置文档（会话无关超集）。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public final class SitemapConfig {

    private String id;
    private String resourceType;
    private Integer version;
    private String dslVersion;
    private SitemapAppInfo app;
    private String layout;
    private SitemapAuthConfig auth;
    private List<SitemapPageDescriptor> pages = new ArrayList<>();
    private List<SitemapMenuItem> menus = new ArrayList<>();
    private Map<String, SitemapLayoutDefinition> layouts = new LinkedHashMap<>();
}

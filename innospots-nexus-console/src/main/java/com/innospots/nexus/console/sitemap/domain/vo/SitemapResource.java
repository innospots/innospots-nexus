package com.innospots.nexus.console.sitemap.domain.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.console.sitemap.domain.model.SitemapAppInfo;
import com.innospots.nexus.console.sitemap.domain.model.SitemapAuthConfig;
import com.innospots.nexus.console.sitemap.domain.model.SitemapLayoutDefinition;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;
import com.innospots.nexus.console.sitemap.domain.model.SitemapPageDescriptor;

import lombok.Getter;
import lombok.Setter;

/**
 * 对外下发的 sitemap 资源（按会话裁剪后的形态；结构与 {@link com.innospots.nexus.console.sitemap.domain.config.SitemapConfig}
 * 同构，并预留会话动态字段）。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapResource", description = "动态页面 Sitemap 资源")
public final class SitemapResource {

    @Schema(description = "站点 id", required = true, examples = {"site"})
    private String id;

    @Schema(description = "资源类型", required = true, examples = {"sitemap"})
    private String resourceType;

    @Schema(description = "Sitemap 版本")
    private Integer version;

    @Schema(description = "DSL 版本", examples = {"1.0"})
    private String dslVersion;

    @Schema(description = "最近更新时间（ISO-8601，会话渲染时注入）")
    private String updatedAt;

    @Schema(description = "应用元数据")
    private SitemapAppInfo app;

    @Schema(description = "当前用户快照（会话渲染时注入）")
    private Map<String, Object> user = new LinkedHashMap<>();

    @Schema(description = "当前用户权限码（会话渲染时注入）")
    private List<String> permissions = new ArrayList<>();

    @Schema(description = "默认布局键")
    private String layout;

    @Schema(description = "认证配置")
    private SitemapAuthConfig auth;

    @Schema(description = "页面声明列表", required = true)
    private List<SitemapPageDescriptor> pages = new ArrayList<>();

    @Schema(description = "菜单树", required = true)
    private List<SitemapMenuItem> menus = new ArrayList<>();

    @Schema(description = "命名布局 DSL 表", required = true)
    private Map<String, SitemapLayoutDefinition> layouts = new LinkedHashMap<>();
}

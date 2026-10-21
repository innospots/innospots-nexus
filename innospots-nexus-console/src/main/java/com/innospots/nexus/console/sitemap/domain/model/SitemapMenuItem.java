package com.innospots.nexus.console.sitemap.domain.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * meta-sitemap 菜单树节点，与前端 {@code sitemap.yaml} 的 {@code menus[]} 同形。
 *
 * <p>路由在前端 {@code pages[]} 声明；权限由 catalog / navigation 判定，不在此重复。
 * 示例：{@code { id: login, type: page, pageId: login, title: 登录, layout: login, public: true, hidden: true }}</p>
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapMenuItem", description = "meta-sitemap 菜单节点（page | group）")
public final class SitemapMenuItem {

    @Schema(description = "菜单 id", required = true, examples = {"login"})
    private String id;

    @Schema(description = "节点类型：page | group", required = true, examples = {"page"})
    private String type;

    @Schema(description = "关联页面 id（对应前端 pages[].id）", examples = {"login"})
    private String pageId;

    @Schema(description = "显示标题", examples = {"登录"})
    private String title;

    @Schema(description = "布局键（前端 shell，如 login / admin）", examples = {"login"})
    private String layout;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "同级排序")
    private Integer order;

    @Schema(description = "匿名可访问（YAML 键 public）")
    @JsonProperty("public")
    private Boolean publicAccess;

    @Schema(description = "不在侧栏展示（详情页等）")
    private Boolean hidden;

    @Schema(description = "详情页高亮侧栏菜单 id")
    private String activeMenu;

    @Schema(description = "后端预设可见角色（与 roleId 匹配；空不限制）")
    private List<String> roles = new ArrayList<>();

    @Schema(description = "子菜单（type=group）")
    private List<SitemapMenuItem> children = new ArrayList<>();
}

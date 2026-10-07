package com.innospots.nexus.console.sitemap.domain.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Sitemap 菜单树节点（page / group / link）。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapMenuItem", description = "Sitemap 菜单节点")
public final class SitemapMenuItem {

    @Schema(description = "菜单 id", required = true)
    private String id;

    @Schema(description = "节点类型：page | group | link", required = true)
    private String type;

    @Schema(description = "关联页面 id（type=page）")
    private String pageId;

    @Schema(description = "显示标题")
    private String title;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "布局键")
    private String layout;

    @Schema(description = "排序")
    private Integer order;

    @Schema(description = "匿名可访问")
    private Boolean publicAccess;

    @Schema(description = "不在导航中展示")
    private Boolean hidden;

    @Schema(description = "高亮所属菜单 id")
    private String activeMenu;

    @Schema(description = "访问所需权限码")
    private String permission;

    @Schema(description = "外链地址（type=link）")
    private String path;

    @Schema(description = "是否外部链接")
    private Boolean external;

    @Schema(description = "子菜单（type=group）")
    private List<SitemapMenuItem> children = new ArrayList<>();

    @JsonProperty("public")
    public void setPublic(Boolean value) {
        this.publicAccess = value;
    }

    @JsonProperty("public")
    public Boolean getPublic() {
        return publicAccess;
    }
}

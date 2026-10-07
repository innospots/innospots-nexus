package com.innospots.nexus.console.sitemap.domain.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.innospots.nexus.console.ui.spec.PageDsl;

import lombok.Getter;
import lombok.Setter;

/**
 * Sitemap 中的页面声明。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapPageDescriptor", description = "Sitemap 页面声明")
public final class SitemapPageDescriptor {

    @Schema(description = "页面 id", required = true)
    private String id;

    @Schema(description = "路由路径", required = true)
    private String path;

    @Schema(description = "页面标题")
    private String title;

    @Schema(description = "页面版本")
    private Integer version;

    @Schema(description = "匿名可访问")
    private Boolean publicAccess;

    @Schema(description = "是否懒加载（不下发 pageDsl）")
    private Boolean lazy;

    @Schema(description = "访问所需权限码")
    private String permission;

    @Schema(description = "内联页面 DSL（非懒加载时下发）")
    private PageDsl pageDsl;

    /**
     * YAML 字段 {@code public} 的 Jackson 绑定。
     */
    @com.fasterxml.jackson.annotation.JsonProperty("public")
    public void setPublic(Boolean value) {
        this.publicAccess = value;
    }

    /**
     * JSON 序列化字段名 {@code public}。
     */
    @com.fasterxml.jackson.annotation.JsonProperty("public")
    public Boolean getPublic() {
        return publicAccess;
    }
}

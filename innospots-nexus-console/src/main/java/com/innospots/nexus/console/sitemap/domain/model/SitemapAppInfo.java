package com.innospots.nexus.console.sitemap.domain.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * Sitemap 应用展示元数据。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapAppInfo", description = "Sitemap 应用元数据")
public final class SitemapAppInfo {

    @Schema(description = "应用显示名称")
    private String name;
}

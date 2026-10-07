package com.innospots.nexus.console.sitemap.domain.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * 命名布局的 DSL 定义。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapLayoutDefinition", description = "命名布局 DSL")
public final class SitemapLayoutDefinition {

    @Schema(description = "DSL 版本", examples = {"1.0"})
    private String dsl;

    @Schema(description = "布局根节点", required = true)
    private SitemapLayoutNode body;
}

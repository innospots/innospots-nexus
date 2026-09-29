package com.innospots.nexus.console.sitemap.domain.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import lombok.Getter;
import lombok.Setter;

/**
 * Sitemap 认证相关配置。
 */
@Getter
@Setter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(name = "SitemapAuthConfig", description = "Sitemap 认证配置")
public final class SitemapAuthConfig {

    @Schema(description = "登录页路径", examples = {"/login"})
    private String loginPath;
}

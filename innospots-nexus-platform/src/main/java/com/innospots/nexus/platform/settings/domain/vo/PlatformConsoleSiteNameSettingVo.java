package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleSiteNameSettingVo", description = "系统名称设置")
public record PlatformConsoleSiteNameSettingVo(
        @Schema(description = "系统名称", required = true) String siteName,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

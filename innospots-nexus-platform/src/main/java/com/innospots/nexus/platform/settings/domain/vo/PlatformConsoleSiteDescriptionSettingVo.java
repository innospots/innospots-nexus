package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleSiteDescriptionSettingVo", description = "系统简介设置")
public record PlatformConsoleSiteDescriptionSettingVo(
        @Schema(description = "系统简介") String description,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

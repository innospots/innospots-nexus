package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformNavigationLayoutMode;

@Schema(name = "PlatformNavigationLayoutSettingVo", description = "控制台导航布局设置")
public record PlatformNavigationLayoutSettingVo(
        @Schema(description = "导航布局：RAIL_DUAL | SINGLE_GROUP") PlatformNavigationLayoutMode navigationLayoutMode,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

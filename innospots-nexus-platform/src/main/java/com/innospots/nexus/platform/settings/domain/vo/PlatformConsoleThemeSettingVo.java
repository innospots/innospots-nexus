package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformConsoleTheme;

@Schema(name = "PlatformConsoleThemeSettingVo", description = "控制台配色主题设置")
public record PlatformConsoleThemeSettingVo(
        @Schema(description = "配色主题") PlatformConsoleTheme theme,
        @Schema(description = "Theme Studio 皮肤键，与 public/skins 文件名一致") String skinKey,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

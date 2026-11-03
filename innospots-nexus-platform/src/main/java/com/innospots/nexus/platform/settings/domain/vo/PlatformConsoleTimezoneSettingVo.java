package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleTimezoneSettingVo", description = "控制台默认时区设置")
public record PlatformConsoleTimezoneSettingVo(
        @Schema(description = "默认时区", required = true) String timezone,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

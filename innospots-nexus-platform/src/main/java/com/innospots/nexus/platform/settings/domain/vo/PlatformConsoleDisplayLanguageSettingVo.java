package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleDisplayLanguageSettingVo", description = "控制台界面语言设置")
public record PlatformConsoleDisplayLanguageSettingVo(
        @Schema(description = "界面语言", required = true) String language,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

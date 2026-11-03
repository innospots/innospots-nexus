package com.innospots.nexus.platform.settings.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleGeneralSettingsVo", description = "控制台基础系统设置（聚合读）")
public record PlatformConsoleGeneralSettingsVo(
        @Schema(description = "系统名称", required = true) String siteName,
        @Schema(description = "系统简介") String description,
        @Schema(description = "界面语言", required = true) String language,
        @Schema(description = "默认时区", required = true) String timezone) {
}

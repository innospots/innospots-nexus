package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformConsoleTheme;

@Schema(name = "PlatformConsoleThemeUpdateRequest", description = "更新控制台配色主题")
public record PlatformConsoleThemeUpdateRequest(
        @Schema(description = "配色主题", required = true) PlatformConsoleTheme theme) {
}

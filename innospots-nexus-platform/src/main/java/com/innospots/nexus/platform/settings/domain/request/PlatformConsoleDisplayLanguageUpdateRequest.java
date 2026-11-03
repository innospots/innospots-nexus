package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleDisplayLanguageUpdateRequest", description = "更新控制台界面语言")
public record PlatformConsoleDisplayLanguageUpdateRequest(
        @Schema(description = "界面语言", required = true) String language) {
}

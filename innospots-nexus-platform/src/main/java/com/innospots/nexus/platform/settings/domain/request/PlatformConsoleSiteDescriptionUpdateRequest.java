package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleSiteDescriptionUpdateRequest", description = "更新系统简介")
public record PlatformConsoleSiteDescriptionUpdateRequest(
        @Schema(description = "系统简介") String description) {
}

package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleSiteNameUpdateRequest", description = "更新系统名称")
public record PlatformConsoleSiteNameUpdateRequest(
        @Schema(description = "系统名称", required = true) String siteName) {
}

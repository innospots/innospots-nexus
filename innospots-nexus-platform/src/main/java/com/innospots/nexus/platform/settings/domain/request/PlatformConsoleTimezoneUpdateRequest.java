package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformConsoleTimezoneUpdateRequest", description = "更新控制台默认时区")
public record PlatformConsoleTimezoneUpdateRequest(
        @Schema(description = "默认时区", required = true) String timezone) {
}

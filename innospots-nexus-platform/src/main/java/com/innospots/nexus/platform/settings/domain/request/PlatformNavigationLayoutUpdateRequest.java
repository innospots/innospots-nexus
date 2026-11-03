package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformNavigationLayoutMode;

@Schema(name = "PlatformNavigationLayoutUpdateRequest", description = "更新控制台导航布局")
public record PlatformNavigationLayoutUpdateRequest(
        @Schema(
                        description = "导航布局：RAIL_DUAL（左轨双栏）| SINGLE_GROUP（单栏分组）",
                        required = true)
                PlatformNavigationLayoutMode navigationLayoutMode) {
}

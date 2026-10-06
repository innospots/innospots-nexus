package com.innospots.nexus.platform.settings.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;

@Schema(name = "PlatformRegistrationPolicyVo", description = "平台自助注册模式设置")
public record PlatformRegistrationPolicyVo(
        @Schema(description = "当前注册模式") PlatformRegistrationMode registrationMode,
        @Schema(description = "最后更新时间") LocalDateTime updatedAt) {
}

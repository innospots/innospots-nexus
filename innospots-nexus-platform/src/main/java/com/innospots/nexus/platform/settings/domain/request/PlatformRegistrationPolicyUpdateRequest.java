package com.innospots.nexus.platform.settings.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;

@Schema(name = "PlatformRegistrationPolicyUpdateRequest", description = "更新平台自助注册模式")
public record PlatformRegistrationPolicyUpdateRequest(
        @Schema(description = "注册模式：OPEN | INVITE | APPROVAL", required = true)
        PlatformRegistrationMode registrationMode) {
}

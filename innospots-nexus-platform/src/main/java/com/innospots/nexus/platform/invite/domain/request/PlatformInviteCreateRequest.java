package com.innospots.nexus.platform.invite.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;

@Schema(name = "PlatformInviteCreateRequest", description = "创建平台邀请")
public record PlatformInviteCreateRequest(
        @Schema(description = "邮箱；与手机至少一项")
        String email,
        @Schema(description = "手机")
        String mobile,
        @Schema(description = "预置登录名")
        String loginName,
        @Schema(description = "默认角色编码，逗号分隔")
        String defaultRoleCodes,
        @Schema(description = "有效天数，默认 7")
        Integer validityDays,
        @Schema(description = "交付方式", required = true)
        PlatformInviteDeliveryMode deliveryMode,
        @Schema(description = "通知模板语言")
        String locale
) {
}

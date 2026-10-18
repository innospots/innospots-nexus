package com.innospots.nexus.console.auth.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.console.config.RegistrationMode;
import com.innospots.nexus.console.config.RegistrationOtpVerification;

/**
 * 公开注册策略（匿名 REST），供登录/注册页决定路由与表单项显隐。
 *
 * @author Smars
 * @date 2026/10/18
 * @param registrationMode           当前自助注册模式
 * @param otpVerification            注册 OTP 校验方式
 * @param openRegistrationEnabled    是否应展示并允许 {@code /register/open}
 * @param inviteRegistrationEnabled  是否应展示邀请码注册
 * @param approvalRegistrationEnabled 是否应展示待审核注册
 * @see com.innospots.nexus.console.auth.service.RegistrationPolicyService#getPublicPolicy()
 */
@Schema(name = "PublicRegistrationPolicyVo", description = "公开注册策略（供登录/注册页渲染）")
public record PublicRegistrationPolicyVo(
        @Schema(description = "当前自助注册模式", required = true)
        RegistrationMode registrationMode,
        @Schema(description = "注册 OTP 校验方式", required = true)
        RegistrationOtpVerification otpVerification,
        @Schema(description = "是否开放 /register/open")
        boolean openRegistrationEnabled,
        @Schema(description = "是否开放邀请码注册")
        boolean inviteRegistrationEnabled,
        @Schema(description = "是否开放待审核注册")
        boolean approvalRegistrationEnabled
) {
}

package com.innospots.nexus.platform.access.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.domain.request.PlatformOpenRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.support.PlatformRegistrationContactSupport;
import com.innospots.nexus.platform.access.support.PlatformRegistrationOtpSupport;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * OPEN 模式完全开放注册：OTP 验证后直接创建 {@code ACTIVE} 用户。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.endpoint.PlatformPublicOpenRegistrationEndpoint
 * @see com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode#OPEN
 */
@RequiredArgsConstructor
public class PlatformOpenRegistrationService {

    private final PlatformUserService platformUserService;
    private final OtpChallengeService otpChallengeService;
    private final PasswordDecryptor passwordDecryptor;
    private final PlatformRegistrationModeSettingService registrationModeSettingService;

    /**
     * 为 OPEN 注册向联系方式发送 OTP（与提交表单使用相同的 email/mobile 解析规则）。
     *
     * @param request 联系方式与可选 locale
     */
    public void issueVerificationOtp(PlatformRegistrationOtpRequest request) {
        registrationModeSettingService.requireSelfServiceMode(PlatformRegistrationMode.OPEN);
        Objects.requireNonNull(request, "request");
        PlatformRegistrationOtpSupport.issue(
                otpChallengeService,
                OtpPurpose.PLATFORM_OPEN_REGISTRATION,
                request.email(),
                request.mobile(),
                request.contact(),
                request.locale());
    }

    /**
     * 校验 OTP 后创建 {@code ACTIVE} 平台用户。
     *
     * @param request 注册表单与验证码
     * @return 新用户的 {@code platformUserId}
     */
    @Transactional
    public String register(PlatformOpenRegistrationSubmitRequest request) {
        registrationModeSettingService.requireSelfServiceMode(PlatformRegistrationMode.OPEN);
        Objects.requireNonNull(request, "request");
        PlatformRegistrationContactSupport.requireContact(request.email(), request.mobile());
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        Checks.notBlank(request.verificationCode(), "verificationCode");
        String contact = PlatformRegistrationContactSupport.resolveVerificationContact(
                request.email(),
                request.mobile());
        verifyOpenRegistrationCode(contact, request.verificationCode());
        String loginName = PlatformRegistrationContactSupport.resolveOpenRegistrationLoginName(
                request.loginName(),
                request.email(),
                request.mobile());
        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        PlatformUserVo user = platformUserService.createActiveUserWithPassword(
                loginName,
                request.displayName(),
                request.email(),
                request.mobile(),
                rawPassword);
        return user.platformUserId();
    }

    private void verifyOpenRegistrationCode(String contact, String verificationCode) {
        OtpChannel channel = PlatformRegistrationContactSupport.resolveChannel(contact);
        otpChallengeService.verifyOrThrow(new OtpVerifyCommand(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_OPEN_REGISTRATION,
                channel,
                contact,
                verificationCode));
        otpChallengeService.invalidate(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_OPEN_REGISTRATION,
                channel,
                contact);
    }
}

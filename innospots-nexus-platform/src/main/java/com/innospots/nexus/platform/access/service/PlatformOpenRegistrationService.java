package com.innospots.nexus.platform.access.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.service.RegistrationPolicyService;
import com.innospots.nexus.console.config.RegistrationConfig;
import com.innospots.nexus.console.config.RegistrationMode;
import com.innospots.nexus.console.config.RegistrationOtpVerification;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.domain.request.PlatformOpenRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.support.PlatformRegistrationContactSupport;
import com.innospots.nexus.platform.access.support.PlatformRegistrationOtpSupport;
import com.innospots.nexus.platform.access.support.PlatformRegistrationOtpVerificationSupport;
import com.innospots.nexus.platform.support.PlatformRequestTrace;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * OPEN 模式完全开放注册：在 {@link RegistrationMode#OPEN} 下签发 OTP（若配置需要）并创建 {@code ACTIVE} 用户。
 *
 * @author Smars
 * @date 2026/10/18
 * @see RegistrationPolicyService
 * @see com.innospots.nexus.platform.access.endpoint.PlatformPublicOpenRegistrationEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformOpenRegistrationService {

    /** 平台用户创建与密码写入。 */
    private final PlatformUserService platformUserService;

    /** OTP 挑战发放与校验。 */
    private final OtpChallengeService otpChallengeService;

    /** 解密前端 RSA 加密密码。 */
    private final PasswordDecryptor passwordDecryptor;

    /** 注册模式与 {@link RegistrationConfig#getOtpVerification()} 门禁。 */
    private final RegistrationPolicyService registrationPolicyService;

    public void issueVerificationOtp(PlatformRegistrationOtpRequest request) {
        registrationPolicyService.requireSelfServiceMode(RegistrationMode.OPEN);
        Objects.requireNonNull(request, "request");
        RegistrationConfig registrationConfig = registrationPolicyService.registrationConfig();
        PlatformRegistrationOtpVerificationSupport.requireOtpAllowed(registrationConfig);
        log.info(
                "traceId={} platform OPEN registration OTP issue contact={}",
                PlatformRequestTrace.currentTraceId(),
                PlatformRequestTrace.describeContact(request.email(), request.mobile()));
        PlatformRegistrationOtpVerificationSupport.requireContactForVerification(
                registrationConfig,
                request.email(),
                request.mobile());
        String email = registrationConfig.getOtpVerification() == RegistrationOtpVerification.EMAIL
                ? request.email()
                : null;
        String mobile = registrationConfig.getOtpVerification() == RegistrationOtpVerification.MOBILE
                ? request.mobile()
                : null;
        PlatformRegistrationOtpSupport.issue(
                otpChallengeService,
                OtpPurpose.PLATFORM_OPEN_REGISTRATION,
                email,
                mobile,
                request.contact(),
                request.locale());
    }

    @Transactional
    public String register(PlatformOpenRegistrationSubmitRequest request) {
        registrationPolicyService.requireSelfServiceMode(RegistrationMode.OPEN);
        Objects.requireNonNull(request, "request");
        RegistrationConfig registrationConfig = registrationPolicyService.registrationConfig();
        String traceId = PlatformRequestTrace.currentTraceId();
        log.info(
                "traceId={} platform OPEN registration submit login={} contact={}",
                traceId,
                PlatformRequestTrace.safeLogin(request.loginName()),
                PlatformRequestTrace.describeContact(request.email(), request.mobile()));
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        if (registrationConfig.isOtpRequired()) {
            PlatformRegistrationOtpVerificationSupport.requireContactForVerification(
                    registrationConfig,
                    request.email(),
                    request.mobile());
            PlatformRegistrationOtpVerificationSupport.requireVerificationCodeIfNeeded(
                    registrationConfig,
                    request.verificationCode());
            String contact = PlatformRegistrationContactSupport.resolveVerificationContact(
                    request.email(),
                    request.mobile());
            verifyOpenRegistrationCode(contact, request.verificationCode());
        } else {
            Checks.notBlank(request.loginName(), "loginName");
        }
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
        log.info(
                "traceId={} platform OPEN registration completed platformUserId={} login={}",
                traceId,
                user.platformUserId(),
                PlatformRequestTrace.safeLogin(user.loginName()));
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

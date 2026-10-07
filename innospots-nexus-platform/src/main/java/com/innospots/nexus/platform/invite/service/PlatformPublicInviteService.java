package com.innospots.nexus.platform.invite.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.invite.domain.entity.PlatformInviteEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteAcceptRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteActivateByCodeRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteOtpRequest;
import com.innospots.nexus.platform.invite.domain.vo.PlatformInvitePreviewVo;
import com.innospots.nexus.platform.invite.operator.PlatformInviteOperator;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

/**
 * 公开邀请注册：链接 token 接受与注册页邀请码激活（INVITE 模式门禁仅作用于邀请码路径）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint
 */
@RequiredArgsConstructor
public class PlatformPublicInviteService {

    private final PlatformInviteOperator inviteOperator;
    private final PlatformUserService platformUserService;
    private final OtpChallengeService otpChallengeService;
    private final PasswordDecryptor passwordDecryptor;
    private final PlatformRegistrationModeSettingService registrationModeSettingService;
    private final PlatformUserRoleProvisioner platformUserRoleProvisioner;

    /**
     * 匿名预览邀请令牌（掩码联系方式）。
     *
     * @param token 邀请 URL 令牌
     * @return 预览信息；令牌无效时由 Operator 抛出业务异常
     */
    public PlatformInvitePreviewVo previewInvite(String token) {
        PlatformInviteEntity entity = inviteOperator.requirePendingByToken(token);
        return new PlatformInvitePreviewVo(
                maskEmail(entity.getEmail()),
                maskMobile(entity.getMobile()),
                entity.getLoginName(),
                entity.getExpiresAt(),
                entity.getStatus());
    }

    /**
     * 为邀请链接接受页签发 OTP（在线交付邀请）。
     *
     * @param token   邀请令牌
     * @param request 可选 locale
     */
    public void issueInviteAcceptOtp(String token, PlatformInviteOtpRequest request) {
        PlatformInviteEntity invite = inviteOperator.requirePendingByToken(token);
        OtpChannel channel = resolveChannel(invite.getEmail(), invite.getMobile());
        String destination = channel == OtpChannel.EMAIL ? invite.getEmail() : invite.getMobile();
        String locale = request == null ? null : request.locale();
        otpChallengeService.issue(new OtpIssueCommand(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_INVITE_ACCEPT,
                channel,
                destination,
                locale));
    }

    /**
     * 通过邮件/链接令牌完成设密并开通（不受 {@code registration.mode} 限制）。
     *
     * @param token   邀请令牌
     * @param request 设密与可选 OTP、登录名
     * @return 新或关联的 {@code platformUserId}
     */
    @Transactional
    public String acceptInvite(String token, PlatformInviteAcceptRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        PlatformInviteEntity invite = inviteOperator.requirePendingByToken(token);
        if (invite.deliveryModeEnum() == PlatformInviteDeliveryMode.ONLINE) {
            verifyInviteAcceptCode(invite, request.verificationCode());
        }
        return completeInviteActivation(invite, request.loginName(), request.encryptedPassword());
    }

    /**
     * 注册页凭邀请码开通（要求当前 {@link PlatformRegistrationMode#INVITE}）。
     *
     * @param request 邀请码、联系方式校验与设密
     * @return {@code platformUserId}
     */
    @Transactional
    public String activateByInviteCode(PlatformInviteActivateByCodeRequest request) {
        registrationModeSettingService.requireSelfServiceMode(PlatformRegistrationMode.INVITE);
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.inviteCode(), "inviteCode");
        Checks.notBlank(request.contact(), "contact");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        PlatformInviteEntity invite = inviteOperator.requirePendingByCode(request.inviteCode());
        assertContactMatches(invite, request.contact());
        return completeInviteActivation(invite, request.loginName(), request.encryptedPassword());
    }

    /**
     * 创建 ACTIVE 用户、按邀请单授予默认角色（非空时），并将邀请单标记为已接受。
     *
     * <p>登录名或联系方式与已有用户冲突时由 {@link PlatformUserService} 抛出异常，邀请单保持
     * {@link com.innospots.nexus.platform.invite.domain.enums.PlatformInviteStatus#PENDING}。</p>
     */
    private String completeInviteActivation(PlatformInviteEntity invite, String loginName, String encryptedPassword) {
        String resolvedLoginName = resolveLoginName(invite, loginName);
        String rawPassword = passwordDecryptor.decrypt(encryptedPassword);
        PlatformUserVo user = platformUserService.createActiveUserWithPassword(
                resolvedLoginName,
                invite.getEmail(),
                invite.getMobile(),
                rawPassword);
        platformUserRoleProvisioner.assignDefaultRolesIfPresent(user.platformUserId(), invite.getDefaultRoleCodes());
        inviteOperator.markAccepted(invite, user.platformUserId());
        return user.platformUserId();
    }

    private void verifyInviteAcceptCode(PlatformInviteEntity invite, String verificationCode) {
        Checks.notBlank(verificationCode, "verificationCode");
        OtpChannel channel = resolveChannel(invite.getEmail(), invite.getMobile());
        String destination = channel == OtpChannel.EMAIL ? invite.getEmail() : invite.getMobile();
        otpChallengeService.verifyOrThrow(new OtpVerifyCommand(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_INVITE_ACCEPT,
                channel,
                destination,
                verificationCode));
        otpChallengeService.invalidate(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_INVITE_ACCEPT,
                channel,
                destination);
    }

    private void assertContactMatches(PlatformInviteEntity invite, String contact) {
        Checks.notBlank(contact, "contact");
        String normalized = contact.trim();
        boolean emailMatch = invite.getEmail() != null
                && invite.getEmail().equalsIgnoreCase(normalized);
        boolean mobileMatch = invite.getMobile() != null
                && invite.getMobile().replace(" ", "").equals(normalized.replace(" ", ""));
        if (!emailMatch && !mobileMatch) {
            if (inviteOperator.registerCodeFailure(invite)) {
                // 连续失败达阈值后锁定邀请码，降低撞库风险
                throw NexusException.build(PlatformInviteStatusCode.INVITE_CODE_LOCKED);
            }
            throw NexusException.build(PlatformInviteStatusCode.INVITE_CONTACT_MISMATCH);
        }
    }

    private static String resolveLoginName(PlatformInviteEntity invite, String loginName) {
        if (loginName != null && !loginName.isBlank()) {
            return loginName.trim();
        }
        if (invite.getLoginName() != null && !invite.getLoginName().isBlank()) {
            return invite.getLoginName().trim();
        }
        if (invite.getEmail() != null && invite.getEmail().contains("@")) {
            return invite.getEmail().substring(0, invite.getEmail().indexOf('@'));
        }
        throw NexusException.build(PlatformInviteStatusCode.INVITE_CONTACT_REQUIRED);
    }

    private static OtpChannel resolveChannel(String email, String mobile) {
        if (email != null && !email.isBlank()) {
            return OtpChannel.EMAIL;
        }
        if (mobile != null && !mobile.isBlank()) {
            return OtpChannel.MOBILE;
        }
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER.fullCode(), "contact is required");
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return null;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 2);
    }
}

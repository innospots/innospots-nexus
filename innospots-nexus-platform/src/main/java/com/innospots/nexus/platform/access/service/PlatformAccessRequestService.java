package com.innospots.nexus.platform.access.service;

import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.domain.entity.PlatformAccessRequestEntity;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestApproveRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestPageRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestRejectRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.domain.vo.PlatformAccessRequestVo;
import com.innospots.nexus.platform.access.operator.PlatformAccessRequestOperator;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;
import com.innospots.nexus.platform.access.support.PlatformRegistrationContactSupport;
import com.innospots.nexus.platform.access.support.PlatformRegistrationOtpSupport;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * APPROVAL 模式自助注册与审批编排：OTP、待审用户、审批通过/拒绝。
 *
 * <p>仅在 {@link PlatformRegistrationMode#APPROVAL} 下接受公开提交；审批通过将已有用户
 * {@code PENDING_APPROVAL → ACTIVE}，不创建邀请单。</p>
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.endpoint.PlatformAccessRequestEndpoint
 * @see com.innospots.nexus.platform.access.endpoint.PlatformPublicAccessRegistrationEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformAccessRequestService {

    private final PlatformAccessRequestOperator accessRequestOperator;
    private final PlatformUserService platformUserService;
    private final OtpChallengeService otpChallengeService;
    private final PasswordDecryptor passwordDecryptor;
    private final PlatformRegistrationModeSettingService registrationModeSettingService;

    /**
     * 为 APPROVAL 公开注册签发联系方式 OTP（受当前注册模式约束）。
     *
     * <p>同一联系方式在 {@link com.innospots.nexus.console.credential.otp.policy.OtpPolicy#DEFAULT}
     * 重发冷却（默认 60 秒）内不可重复下发。</p>
     *
     * @param request 联系方式（email/mobile 与提交表单一致）及模板语言
     * @throws NexusException 冷却未到时 {@link PlatformAccessStatusCode#ACCESS_OTP_RESEND_TOO_FREQUENT}
     */
    public void issueAccessRegistrationOtp(PlatformRegistrationOtpRequest request) {
        registrationModeSettingService.requireSelfServiceMode(PlatformRegistrationMode.APPROVAL);
        Objects.requireNonNull(request, "request");
        issueAccessVerificationOtp(request);
    }

    /**
     * 签发 {@link OtpPurpose#PLATFORM_ACCESS_VERIFY} 验证码（不校验注册模式，供内部复用）。
     *
     * @param request 联系方式与 locale
     * @throws NexusException 冷却未到时 {@link PlatformAccessStatusCode#ACCESS_OTP_RESEND_TOO_FREQUENT}
     */
    public void issueAccessVerificationOtp(PlatformRegistrationOtpRequest request) {
        Objects.requireNonNull(request, "request");
        PlatformRegistrationOtpSupport.issue(
                otpChallengeService,
                OtpPurpose.PLATFORM_ACCESS_VERIFY,
                request.email(),
                request.mobile(),
                request.contact(),
                request.locale());
    }

    /**
     * 提交待审注册：OTP 通过后创建 {@code PENDING_APPROVAL} 用户并写入申请单。
     *
     * @param request 登录名、联系方式、加密密码与验证码
     * @return 申请视图（含 {@code platformUserId}）
     */
    @Transactional
    public PlatformAccessRequestVo submitAccessRegistration(PlatformAccessRegistrationSubmitRequest request) {
        registrationModeSettingService.requireSelfServiceMode(PlatformRegistrationMode.APPROVAL);
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.loginName(), "loginName");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        PlatformRegistrationContactSupport.requireContact(request.email(), request.mobile());
        verifyAccessCode(request);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        String applicantName = resolveApplicantName(request.displayName(), request.loginName());
        // 先建用户并 enroll 密码，审批通过时仅改状态，无需二次设密或发邀请
        PlatformUserVo user = platformUserService.createPendingApprovalUserWithPassword(
                request.loginName(),
                request.displayName(),
                request.email(),
                request.mobile(),
                rawPassword);

        PlatformAccessRequestEntity entity = new PlatformAccessRequestEntity();
        entity.setApplicantName(applicantName);
        entity.setLoginName(user.loginName());
        entity.setPlatformUserId(user.platformUserId());
        entity.setEmail(request.email());
        entity.setMobile(request.mobile());
        entity.setDescription(request.description());
        entity.setStatus(com.innospots.nexus.platform.access.domain.enums.PlatformAccessRequestStatus.PENDING_APPROVAL.name());
        accessRequestOperator.insert(entity);
        log.info("Submitted platform access registration {} for user {}", entity.getAccessRequestId(), user.platformUserId());
        return toVo(entity);
    }

    /**
     * 管理端分页查询注册申请。
     *
     * @param request 分页与状态筛选
     * @return 申请视图分页
     */
    public PageResult<PlatformAccessRequestVo> pageAccessRequests(PlatformAccessRequestPageRequest request) {
        PageResult<PlatformAccessRequestEntity> page = accessRequestOperator.page(request);
        List<PlatformAccessRequestVo> records = page.records().stream().map(this::toVo).toList();
        return PageResult.of(records, page.pageNo(), page.pageSize(), page.total());
    }

    /**
     * 按 ID 查询单条申请。
     *
     * @param accessRequestId 申请 ID
     * @return 申请视图
     */
    public PlatformAccessRequestVo getAccessRequest(String accessRequestId) {
        return toVo(accessRequestOperator.requireById(accessRequestId));
    }

    /**
     * 审批通过：激活已关联的待审用户，不创建邀请。
     *
     * @param accessRequestId 申请 ID
     * @param request         预留角色等扩展字段
     * @return 已激活的平台用户
     */
    @Transactional
    public PlatformUserVo approveAccessRequest(String accessRequestId, PlatformAccessRequestApproveRequest request) {
        PlatformAccessRequestEntity entity = accessRequestOperator.requireById(accessRequestId);
        accessRequestOperator.requirePendingApproval(entity);
        Objects.requireNonNull(request, "request");
        Checks.notBlank(entity.getPlatformUserId(), "platformUserId");
        PlatformUserVo user = platformUserService.activatePendingApprovalUser(entity.getPlatformUserId());
        accessRequestOperator.markApproved(entity, currentReviewer());
        log.info("Approved access request {} for user {}", accessRequestId, user.platformUserId());
        return user;
    }

    /**
     * 审批拒绝：禁用关联用户并更新申请状态。
     *
     * @param accessRequestId 申请 ID
     * @param request         可选拒绝原因
     */
    @Transactional
    public void rejectAccessRequest(String accessRequestId, PlatformAccessRequestRejectRequest request) {
        PlatformAccessRequestEntity entity = accessRequestOperator.requireById(accessRequestId);
        accessRequestOperator.requirePendingApproval(entity);
        if (entity.getPlatformUserId() != null && !entity.getPlatformUserId().isBlank()) {
            // 拒绝后保持不可登录（DISABLED），保留行以便审计
            platformUserService.disableUserAfterRegistrationRejected(entity.getPlatformUserId());
        }
        accessRequestOperator.markRejected(
                entity,
                request == null ? null : request.rejectReason(),
                currentReviewer());
    }

    private void verifyAccessCode(PlatformAccessRegistrationSubmitRequest request) {
        Checks.notBlank(request.verificationCode(), "verificationCode");
        String contact = PlatformRegistrationContactSupport.resolveVerificationContact(
                request.email(),
                request.mobile());
        OtpChannel channel = PlatformRegistrationContactSupport.resolveChannel(contact);
        otpChallengeService.verifyOrThrow(new OtpVerifyCommand(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_ACCESS_VERIFY,
                channel,
                contact,
                request.verificationCode()));
        otpChallengeService.invalidate(
                SecurityRealm.PLATFORM,
                OtpPurpose.PLATFORM_ACCESS_VERIFY,
                channel,
                contact);
    }

    private static String resolveApplicantName(String displayName, String loginName) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }
        return loginName.trim();
    }

    /** 优先取 TLC 平台用户 ID，兼容旧会话键。 */
    private static String currentReviewer() {
        String reviewer = TLC.platformUserId();
        if (reviewer == null || reviewer.isBlank()) {
            reviewer = TLC.getString(TLC.USER_ID);
        }
        return reviewer;
    }

    private PlatformAccessRequestVo toVo(PlatformAccessRequestEntity entity) {
        return new PlatformAccessRequestVo(
                entity.getAccessRequestId(),
                entity.getLoginName(),
                entity.getPlatformUserId(),
                entity.getApplicantName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getRejectReason(),
                entity.getReviewedAt(),
                entity.getCreatedAt());
    }
}

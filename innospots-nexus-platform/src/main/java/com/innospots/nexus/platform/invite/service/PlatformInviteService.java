package com.innospots.nexus.platform.invite.service;

import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.platform.invite.domain.entity.PlatformInviteEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteCreateRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInvitePageRequest;
import com.innospots.nexus.platform.invite.domain.vo.PlatformInviteVo;
import com.innospots.nexus.platform.invite.operator.PlatformInviteOperator;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;
import com.innospots.nexus.platform.invite.support.PlatformInviteLinkBuilder;
import com.innospots.nexus.platform.invite.support.PlatformInviteOtpNotifier;

/**
 * 管理端邀请单生命周期：创建、分页、重发、撤销。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformInviteService {

    private final PlatformInviteOperator inviteOperator;
    private final PlatformInviteOtpNotifier inviteOtpNotifier;
    private final PlatformInviteLinkBuilder inviteLinkBuilder;

    /**
     * 创建待接受邀请；ONLINE 交付时异步推送 OTP/链接。
     *
     * @param request 联系方式、角色、有效期与交付方式
     * @return 邀请 VO（含邀请码与链接）
     */
    @Transactional
    public PlatformInviteVo createInvite(PlatformInviteCreateRequest request) {
        Objects.requireNonNull(request, "request");
        requireContact(request.email(), request.mobile());
        PlatformInviteDeliveryMode deliveryMode = request.deliveryMode() == null
                ? PlatformInviteDeliveryMode.ONLINE
                : request.deliveryMode();
        int validityDays = PlatformInviteOperator.normalizeValidityDays(request.validityDays());
        PlatformInviteEntity entity = inviteOperator.newPendingEntity(
                request.email(),
                request.mobile(),
                request.loginName(),
                request.defaultRoleCodes(),
                deliveryMode,
                validityDays);
        inviteOperator.insert(entity);
        if (deliveryMode == PlatformInviteDeliveryMode.ONLINE) {
            inviteOtpNotifier.deliverInvite(entity, OtpPurpose.PLATFORM_INVITE_DELIVERY, request.locale());
        }
        log.info("Created platform invite {}", entity.getInviteId());
        return toVo(entity);
    }

    /**
     * 分页查询邀请单。
     *
     * @param request 筛选与分页
     * @return 邀请 VO 分页
     */
    public PageResult<PlatformInviteVo> pageInvites(PlatformInvitePageRequest request) {
        PageResult<PlatformInviteEntity> page = inviteOperator.page(request);
        List<PlatformInviteVo> records = page.records().stream().map(this::toVo).toList();
        return PageResult.of(records, page.pageNo(), page.pageSize(), page.total());
    }

    /**
     * 按 ID 查询邀请详情。
     *
     * @param inviteId 邀请 ID
     * @return 邀请 VO
     */
    public PlatformInviteVo getInvite(String inviteId) {
        return toVo(inviteOperator.requireById(inviteId));
    }

    /**
     * 轮换令牌/邀请码并延长有效期；ONLINE 模式重新投递。
     *
     * @param inviteId 邀请 ID
     * @param locale   通知 locale
     * @return 更新后的邀请 VO
     */
    @Transactional
    public PlatformInviteVo resendInvite(String inviteId, String locale) {
        PlatformInviteEntity entity = inviteOperator.requireById(inviteId);
        PlatformInviteOperator.ensurePending(entity);
        int validityDays = PlatformInviteOperator.DEFAULT_VALIDITY_DAYS;
        inviteOperator.rotateCredentialsAndExtend(entity, validityDays);
        if (entity.deliveryModeEnum() == PlatformInviteDeliveryMode.ONLINE) {
            inviteOtpNotifier.deliverInvite(entity, OtpPurpose.PLATFORM_INVITE_DELIVERY, locale);
        }
        log.info("Resent platform invite {}", inviteId);
        return toVo(entity);
    }

    /**
     * 撤销待接受邀请，之后不可再激活。
     *
     * @param inviteId 邀请 ID
     */
    @Transactional
    public void revokeInvite(String inviteId) {
        inviteOperator.markRevoked(inviteOperator.requireById(inviteId));
    }

    PlatformInviteVo toVo(PlatformInviteEntity entity) {
        return new PlatformInviteVo(
                entity.getInviteId(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getLoginName(),
                entity.getDefaultRoleCodes(),
                entity.getStatus(),
                entity.getDeliveryMode(),
                entity.getExpiresAt(),
                entity.getInviteCode(),
                inviteLinkBuilder.buildInviteLink(entity.getInviteToken()),
                entity.getPlatformUserId(),
                entity.getAcceptedAt(),
                entity.getCreatedAt());
    }

    private static void requireContact(String email, String mobile) {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasMobile = mobile != null && !mobile.isBlank();
        if (!hasEmail && !hasMobile) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_CONTACT_REQUIRED);
        }
    }
}

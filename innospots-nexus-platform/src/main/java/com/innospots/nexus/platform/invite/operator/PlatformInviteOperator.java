package com.innospots.nexus.platform.invite.operator;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.invite.dao.PlatformInviteDao;
import com.innospots.nexus.platform.invite.domain.entity.PlatformInviteEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteStatus;
import com.innospots.nexus.platform.invite.domain.request.PlatformInvitePageRequest;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;
import com.innospots.nexus.platform.invite.support.PlatformInviteCredentials;

/**
 * {@code nx_pl_invite} 单表读写与邀请单字段变更。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformInviteOperator {

    public static final int MAX_CODE_FAILURES = 5;
    public static final int DEFAULT_VALIDITY_DAYS = 7;

    private final PlatformInviteDao inviteDao;

    @Transactional
    public void insert(PlatformInviteEntity entity) {
        inviteDao.insert(entity);
    }

    @Transactional
    public void update(PlatformInviteEntity entity) {
        inviteDao.updateById(entity);
    }

    public PlatformInviteEntity requireById(String inviteId) {
        Checks.notBlank(inviteId, "inviteId");
        PlatformInviteEntity entity = inviteDao.selectById(inviteId);
        if (entity == null) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_NOT_FOUND);
        }
        return entity;
    }

    public PlatformInviteEntity requirePendingByToken(String token) {
        Checks.notBlank(token, "token");
        PlatformInviteEntity entity = inviteDao.selectOne(new LambdaQueryWrapper<PlatformInviteEntity>()
                .eq(PlatformInviteEntity::getInviteToken, token));
        return ensurePendingAndFresh(entity);
    }

    public PlatformInviteEntity requirePendingByCode(String inviteCode) {
        Checks.notBlank(inviteCode, "inviteCode");
        String normalized = PlatformInviteCredentials.normalizeInviteCode(inviteCode);
        PlatformInviteEntity entity = inviteDao.selectOne(new LambdaQueryWrapper<PlatformInviteEntity>()
                .eq(PlatformInviteEntity::getInviteCode, normalized));
        return ensurePendingAndFresh(entity);
    }

    public PageResult<PlatformInviteEntity> page(PlatformInvitePageRequest request) {
        PlatformInvitePageRequest pageRequest = request == null ? new PlatformInvitePageRequest() : request;
        LambdaQueryWrapper<PlatformInviteEntity> query = new LambdaQueryWrapper<>();
        if (pageRequest.status() != null) {
            query.eq(PlatformInviteEntity::getStatus, pageRequest.status().name());
        }
        String input = pageRequest.input();
        if (input != null && !input.isBlank()) {
            String keyword = input.trim();
            query.and(wrapper -> wrapper
                    .like(PlatformInviteEntity::getEmail, keyword)
                    .or()
                    .like(PlatformInviteEntity::getMobile, keyword)
                    .or()
                    .like(PlatformInviteEntity::getLoginName, keyword));
        }
        query.orderByDesc(PlatformInviteEntity::getCreatedAt);
        IPage<PlatformInviteEntity> selectedPage = inviteDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<PlatformInviteEntity> records = selectedPage.getRecords();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public PlatformInviteEntity newPendingEntity(
            String email,
            String mobile,
            String loginName,
            String defaultRoleCodes,
            PlatformInviteDeliveryMode deliveryMode,
            int validityDays
    ) {
        PlatformInviteEntity entity = new PlatformInviteEntity();
        entity.setEmail(email);
        entity.setMobile(mobile);
        entity.setLoginName(loginName);
        entity.setDefaultRoleCodes(defaultRoleCodes);
        entity.setDeliveryMode(deliveryMode.name());
        entity.setStatus(PlatformInviteStatus.PENDING.name());
        entity.setExpiresAt(LocalDateTime.now().plusDays(validityDays));
        entity.setCodeFailedAttempts(0);
        rotateCredentials(entity);
        return entity;
    }

    @Transactional
    public void rotateCredentialsAndExtend(PlatformInviteEntity entity, int validityDays) {
        ensurePending(entity);
        rotateCredentials(entity);
        entity.setExpiresAt(LocalDateTime.now().plusDays(validityDays));
        inviteDao.updateById(entity);
    }

    @Transactional
    public void markRevoked(PlatformInviteEntity entity) {
        if (entity.statusEnum() == PlatformInviteStatus.ACCEPTED) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_NOT_PENDING);
        }
        entity.setStatus(PlatformInviteStatus.REVOKED.name());
        entity.setRevokedAt(LocalDateTime.now());
        inviteDao.updateById(entity);
        log.info("Revoked platform invite {}", entity.getInviteId());
    }

    @Transactional
    public void markAccepted(PlatformInviteEntity entity, String platformUserId) {
        ensurePending(entity);
        entity.setStatus(PlatformInviteStatus.ACCEPTED.name());
        entity.setAcceptedAt(LocalDateTime.now());
        entity.setPlatformUserId(platformUserId);
        inviteDao.updateById(entity);
    }

    /**
     * 邀请码校验失败时递增计数；达到上限则撤销邀请单。
     *
     * @return 是否因失败次数过多已锁定
     */
    @Transactional
    public boolean registerCodeFailure(PlatformInviteEntity entity) {
        entity.setCodeFailedAttempts(entity.getCodeFailedAttempts() + 1);
        if (entity.getCodeFailedAttempts() >= MAX_CODE_FAILURES) {
            entity.setStatus(PlatformInviteStatus.REVOKED.name());
            entity.setRevokedAt(LocalDateTime.now());
        }
        inviteDao.updateById(entity);
        return entity.getCodeFailedAttempts() >= MAX_CODE_FAILURES;
    }

    public static void ensurePending(PlatformInviteEntity entity) {
        if (entity.statusEnum() != PlatformInviteStatus.PENDING) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_NOT_PENDING);
        }
    }

    public static int normalizeValidityDays(Integer validityDays) {
        if (validityDays == null || validityDays < 1) {
            return DEFAULT_VALIDITY_DAYS;
        }
        return validityDays;
    }

    public static void rotateCredentials(PlatformInviteEntity entity) {
        entity.setInviteToken(PlatformInviteCredentials.newToken());
        entity.setInviteCode(PlatformInviteCredentials.newInviteCode());
    }

    private PlatformInviteEntity ensurePendingAndFresh(PlatformInviteEntity entity) {
        if (entity == null) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_NOT_FOUND);
        }
        if (entity.statusEnum() == PlatformInviteStatus.REVOKED) {
            throw NexusException.build(PlatformInviteStatusCode.INVITE_NOT_PENDING);
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            if (entity.statusEnum() == PlatformInviteStatus.PENDING) {
                entity.setStatus(PlatformInviteStatus.EXPIRED.name());
                inviteDao.updateById(entity);
            }
            throw NexusException.build(PlatformInviteStatusCode.INVITE_EXPIRED);
        }
        ensurePending(entity);
        return entity;
    }
}

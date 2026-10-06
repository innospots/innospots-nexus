package com.innospots.nexus.platform.access.operator;

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
import com.innospots.nexus.platform.access.dao.PlatformAccessRequestDao;
import com.innospots.nexus.platform.access.domain.entity.PlatformAccessRequestEntity;
import com.innospots.nexus.platform.access.domain.enums.PlatformAccessRequestStatus;
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRequestPageRequest;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;

/**
 * {@code nx_pl_access_request} 单表读写；不含 OTP 与用户状态编排。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.service.PlatformAccessRequestService
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformAccessRequestOperator {

    private final PlatformAccessRequestDao accessRequestDao;

    /**
     * 插入新的待审申请记录。
     *
     * @param entity 待持久化实体（ID 由框架生成）
     */
    @Transactional
    public void insert(PlatformAccessRequestEntity entity) {
        accessRequestDao.insert(entity);
    }

    /** 按主键更新申请行。 */
    @Transactional
    public void update(PlatformAccessRequestEntity entity) {
        accessRequestDao.updateById(entity);
    }

    /**
     * 加载申请；不存在时抛出 {@link PlatformAccessStatusCode#ACCESS_REQUEST_NOT_FOUND}。
     *
     * @param accessRequestId 申请 ID
     * @return 实体
     */
    public PlatformAccessRequestEntity requireById(String accessRequestId) {
        Checks.notBlank(accessRequestId, "accessRequestId");
        PlatformAccessRequestEntity entity = accessRequestDao.selectById(accessRequestId);
        if (entity == null) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_REQUEST_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 分页查询注册待审申请。
     *
     * @param request 分页与可选状态筛选
     * @return 实体分页结果
     */
    public PageResult<PlatformAccessRequestEntity> page(PlatformAccessRequestPageRequest request) {
        PlatformAccessRequestPageRequest pageRequest = request == null ? new PlatformAccessRequestPageRequest() : request;
        LambdaQueryWrapper<PlatformAccessRequestEntity> query = new LambdaQueryWrapper<>();
        if (pageRequest.status() != null) {
            query.eq(PlatformAccessRequestEntity::getStatus, pageRequest.status().name());
        }
        query.orderByDesc(PlatformAccessRequestEntity::getCreatedAt);
        IPage<PlatformAccessRequestEntity> selectedPage = accessRequestDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<PlatformAccessRequestEntity> records = selectedPage.getRecords();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    /**
     * 断言申请仍处于待审批状态。
     *
     * @param entity 申请实体
     * @throws NexusException 非 {@code PENDING_APPROVAL} 时
     */
    public void requirePendingApproval(PlatformAccessRequestEntity entity) {
        if (entity.statusEnum() != PlatformAccessRequestStatus.PENDING_APPROVAL) {
            throw NexusException.build(PlatformAccessStatusCode.ACCESS_REQUEST_NOT_PENDING);
        }
    }

    /**
     * 标记申请已通过（用户激活由 Service 负责）。
     *
     * @param entity     待更新实体
     * @param reviewedBy 审批人平台用户 ID
     */
    @Transactional
    public void markApproved(PlatformAccessRequestEntity entity, String reviewedBy) {
        entity.setStatus(PlatformAccessRequestStatus.APPROVED.name());
        entity.setReviewedAt(LocalDateTime.now());
        entity.setReviewedBy(reviewedBy);
        accessRequestDao.updateById(entity);
    }

    /**
     * 标记申请已拒绝并记录原因。
     *
     * @param entity       待更新实体
     * @param rejectReason 可选拒绝说明
     * @param reviewedBy   审批人平台用户 ID
     */
    @Transactional
    public void markRejected(PlatformAccessRequestEntity entity, String rejectReason, String reviewedBy) {
        entity.setStatus(PlatformAccessRequestStatus.REJECTED.name());
        entity.setRejectReason(rejectReason);
        entity.setReviewedAt(LocalDateTime.now());
        entity.setReviewedBy(reviewedBy);
        accessRequestDao.updateById(entity);
        log.info("Rejected platform access request {}", entity.getAccessRequestId());
    }
}

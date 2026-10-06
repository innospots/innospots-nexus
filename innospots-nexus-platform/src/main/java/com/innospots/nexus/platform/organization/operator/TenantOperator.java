package com.innospots.nexus.platform.organization.operator;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.organization.dao.TenantDao;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantStatus;
import com.innospots.nexus.platform.organization.domain.enums.TenantType;
import com.innospots.nexus.platform.organization.domain.request.TenantPageRequest;
import com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode;

/**
 * {@code nx_pl_tenant} 的插入、更新、按主键加载与条件分页。
 * <p>不负责企业档案或跨表事务；由 {@link com.innospots.nexus.platform.organization.service.PlatformTenantService} 编排。</p>
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.dao.TenantDao
 * @see com.innospots.nexus.platform.organization.domain.entity.TenantEntity
 */
@Slf4j
@RequiredArgsConstructor
public class TenantOperator {

    private final TenantDao tenantDao;

    /**
     * 插入新租户并规范化形态、状态与必填字段。
     *
     * @param tenant 待持久化实体；成功后填充 {@code tenantId}
     */
    @Transactional
    public void insert(TenantEntity tenant) {
        normalizeForInsert(tenant);
        ensureTenantCodeAvailable(tenant.getTenantCode());
        tenantDao.insert(tenant);
        log.info("Created tenant {} with code {}", tenant.getTenantId(), tenant.getTenantCode());
    }

    /**
     * 按主键更新租户行。
     *
     * @param tenant 含 {@code tenantId} 的实体
     */
    @Transactional
    public void update(TenantEntity tenant) {
        Checks.notNull(tenant, "tenant");
        Checks.notBlank(tenant.getTenantId(), "tenantId");
        tenantDao.updateById(tenant);
    }

    /**
     * 按主键加载租户，不存在时抛出 {@link com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode#PLATFORM_TENANT_NOT_FOUND}。
     *
     * @param tenantId 租户标识
     * @return 持久化实体
     */
    public TenantEntity requireById(String tenantId) {
        Checks.notBlank(tenantId, "tenantId");
        TenantEntity entity = tenantDao.selectById(tenantId);
        if (entity == null) {
            throw NexusException.build(PlatformOrganizationStatusCode.PLATFORM_TENANT_NOT_FOUND);
        }
        return entity;
    }

    /**
     * 按名称/编码模糊、状态与组织形态筛选并分页。
     *
     * @param request 查询条件；{@code null} 时仅默认分页
     * @return 实体分页（按创建时间倒序）
     */
    public PageResult<TenantEntity> page(TenantPageRequest request) {
        TenantPageRequest pageRequest = request == null ? new TenantPageRequest() : request;
        LambdaQueryWrapper<TenantEntity> query = new LambdaQueryWrapper<>();
        if (pageRequest.status() != null) {
            query.eq(TenantEntity::getStatus, pageRequest.status().name());
        }
        if (pageRequest.tenantType() != null) {
            query.eq(TenantEntity::getTenantType, pageRequest.tenantType().name());
        }
        String input = pageRequest.input();
        if (input != null && !input.isBlank()) {
            String keyword = input.trim();
            query.and(wrapper -> wrapper
                    .like(TenantEntity::getTenantName, keyword)
                    .or()
                    .like(TenantEntity::getTenantCode, keyword));
        }
        query.orderByDesc(TenantEntity::getCreatedAt);
        IPage<TenantEntity> selectedPage = tenantDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<TenantEntity> records = selectedPage.getRecords();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    /**
     * 校验租户编码全局唯一（对应 {@code uk_nx_pl_tenant_code}）。
     *
     * @param tenantCode 待使用的编码
     */
    public void ensureTenantCodeAvailable(String tenantCode) {
        Checks.notBlank(tenantCode, "tenantCode");
        LambdaQueryWrapper<TenantEntity> query = new LambdaQueryWrapper<>();
        query.eq(TenantEntity::getTenantCode, tenantCode.trim());
        if (tenantDao.selectCount(query) > 0) {
            throw NexusException.build(PlatformOrganizationStatusCode.TENANT_CODE_DUPLICATED);
        }
    }

    private static void normalizeForInsert(TenantEntity tenant) {
        Checks.notNull(tenant, "tenant");
        requireText(tenant.getTenantName(), "tenantName");
        requireText(tenant.getTenantCode(), "tenantCode");
        requireTenantType(tenant.getTenantType());
        if (tenant.getStatus() == null || tenant.getStatus().isBlank()) {
            tenant.setStatus(TenantStatus.ACTIVE.name());
        } else {
            requireTenantStatus(tenant.getStatus());
        }
        tenant.setTenantType(TenantType.valueOf(tenant.getTenantType().trim().toUpperCase()).name());
        tenant.setTenantCode(tenant.getTenantCode().trim());
        tenant.setTenantName(tenant.getTenantName().trim());
    }

    /**
     * 校验状态字符串是否为合法的 {@link TenantStatus} 名称。
     *
     * @param status 待校验状态
     */
    public static void requireTenantStatus(String status) {
        requireText(status, "status");
        try {
            TenantStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "status is invalid");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }

    private static void requireTenantType(String tenantType) {
        requireText(tenantType, "tenantType");
        try {
            TenantType.valueOf(tenantType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    "tenantType is invalid");
        }
    }
}

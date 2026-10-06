package com.innospots.nexus.platform.organization.operator;

import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.organization.dao.EnterpriseDao;
import com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity;
import com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode;

/**
 * {@code nx_pl_enterprise} 的按租户查询与插入/更新（与 {@code nx_pl_tenant} 一对一）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.platform.organization.dao.EnterpriseDao
 * @see com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity
 */
@Slf4j
@RequiredArgsConstructor
public class EnterpriseOperator {

    private final EnterpriseDao enterpriseDao;

    /**
     * 按租户 ID 查找企业档案（可能不存在）。
     *
     * @param tenantId 租户标识
     * @return 档案实体或空
     */
    public Optional<EnterpriseEntity> findByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<EnterpriseEntity> query = new LambdaQueryWrapper<>();
        query.eq(EnterpriseEntity::getTenantId, tenantId);
        return Optional.ofNullable(enterpriseDao.selectOne(query));
    }

    /**
     * 按租户 ID 加载企业档案，不存在时抛出 {@link com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode#ENTERPRISE_PROFILE_NOT_FOUND}。
     *
     * @param tenantId 租户标识
     * @return 档案实体
     */
    public EnterpriseEntity requireByTenantId(String tenantId) {
        return findByTenantId(tenantId)
                .orElseThrow(() -> NexusException.build(PlatformOrganizationStatusCode.ENTERPRISE_PROFILE_NOT_FOUND));
    }

    /**
     * 为租户插入企业档案；同一租户已存在档案时冲突。
     *
     * @param enterprise 含 {@code tenantId} 与法定名称的实体
     */
    @Transactional
    public void insert(EnterpriseEntity enterprise) {
        Checks.notNull(enterprise, "enterprise");
        requireText(enterprise.getLegalName(), "legalName");
        Checks.notBlank(enterprise.getTenantId(), "tenantId");
        if (findByTenantId(enterprise.getTenantId()).isPresent()) {
            throw NexusException.build(PlatformOrganizationStatusCode.ENTERPRISE_PROFILE_ALREADY_EXISTS);
        }
        enterpriseDao.insert(enterprise);
        log.info("Created enterprise profile {} for tenant {}", enterprise.getEnterpriseId(), enterprise.getTenantId());
    }

    /**
     * 按主键更新企业档案。
     *
     * @param enterprise 含 {@code enterpriseId} 的实体
     */
    @Transactional
    public void update(EnterpriseEntity enterprise) {
        Checks.notNull(enterprise, "enterprise");
        Checks.notBlank(enterprise.getEnterpriseId(), "enterpriseId");
        requireText(enterprise.getLegalName(), "legalName");
        enterpriseDao.updateById(enterprise);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }
}

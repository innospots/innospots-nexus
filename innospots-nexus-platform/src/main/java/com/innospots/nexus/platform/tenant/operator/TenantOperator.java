package com.innospots.nexus.platform.tenant.operator;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.platform.enterprise.dao.EnterpriseDao;
import com.innospots.nexus.platform.enterprise.domain.entity.EnterpriseEntity;
import com.innospots.nexus.platform.tenant.dao.TenantDao;
import com.innospots.nexus.platform.tenant.domain.entity.TenantEntity;
import com.innospots.nexus.platform.tenant.domain.enums.TenantStatus;

/**
 * 持久化平台租户及其一对一企业档案。
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class TenantOperator {

    private final TenantDao tenantDao;
    private final EnterpriseDao enterpriseDao;

    /**
     * 在一个事务中创建租户及其企业档案。
     *
     * @param tenant     租户身份字段
     * @param enterprise 企业法定档案；插入后填充 {@code tenantId}
     * @return persisted 租户
     */
    @Transactional
    public TenantEntity create(TenantEntity tenant, EnterpriseEntity enterprise) {
        requireText(tenant == null ? null : tenant.getTenantName(), "tenantName");
        requireText(tenant == null ? null : tenant.getTenantCode(), "tenantCode");
        requireText(enterprise == null ? null : enterprise.getLegalName(), "legalName");

        if (tenant.getStatus() == null || tenant.getStatus().isBlank()) {
            tenant.setStatus(TenantStatus.ACTIVE.name());
        }
        tenantDao.insert(tenant);
        enterprise.setTenantId(tenant.getTenantId());
        enterpriseDao.insert(enterprise);
        log.info("Created tenant {} with code {}", tenant.getTenantId(), tenant.getTenantCode());
        return tenant;
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }
}

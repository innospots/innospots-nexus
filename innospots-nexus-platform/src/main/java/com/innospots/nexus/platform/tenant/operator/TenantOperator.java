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
 * Persists platform tenants and their one-to-one enterprise profiles.
 */
@Slf4j
@RequiredArgsConstructor
public class TenantOperator {

    private final TenantDao tenantDao;
    private final EnterpriseDao enterpriseDao;

    /**
     * Creates a tenant and its enterprise profile in one transaction.
     *
     * @param tenant     tenant identity fields
     * @param enterprise enterprise legal profile; {@code tenantId} is filled after insert
     * @return persisted tenant
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

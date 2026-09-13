package com.innospots.nexus.platform.scope;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.console.scope.api.TenantScopeDirectory;
import com.innospots.nexus.console.scope.domain.model.TenantScope;
import com.innospots.nexus.platform.tenant.dao.TenantDao;
import com.innospots.nexus.platform.tenant.domain.entity.TenantEntity;

/**
 * Platform-backed tenant scope directory.
 */
@RequiredArgsConstructor
public class PlatformTenantScopeDirectory implements TenantScopeDirectory {

    private final TenantDao tenantDao;

    @Override
    public Optional<TenantScope> findByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }
        TenantEntity tenant = tenantDao.selectById(tenantId);
        if (tenant == null) {
            return Optional.empty();
        }
        BasicStatus status = toBasicStatus(tenant.getStatus());
        TenantSnapshot tenantSnapshot = new TenantSnapshot(
                tenant.getTenantId(),
                tenant.getTenantCode(),
                tenant.getTenantName(),
                status);
        OrganizationSnapshot organizationSnapshot = new OrganizationSnapshot(
                tenant.getTenantId(),
                tenant.getTenantCode(),
                tenant.getTenantName(),
                null,
                null,
                null,
                status);
        return Optional.of(new TenantScope(tenantSnapshot, organizationSnapshot));
    }

    private static BasicStatus toBasicStatus(String status) {
        if (status == null || status.isBlank()) {
            return BasicStatus.ENABLED;
        }
        if ("ACTIVE".equalsIgnoreCase(status) || "ENABLED".equalsIgnoreCase(status)) {
            return BasicStatus.ENABLED;
        }
        return BasicStatus.DISABLED;
    }
}

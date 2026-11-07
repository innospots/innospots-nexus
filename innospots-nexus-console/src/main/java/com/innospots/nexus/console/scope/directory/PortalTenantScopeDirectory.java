package com.innospots.nexus.console.scope.directory;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.scope.TenantScope;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.console.scope.tenant.dao.TenantDao;
import com.innospots.nexus.console.scope.tenant.domain.entity.TenantEntity;

/**
 * 租户作用域目录：按租户标识解析租户与组织快照。
 *
 * <p>数据源是租户表 {@code nx_tenant}，不依赖组织树，因此本类可以放在 console
 * 而不引入对 portal 的反向依赖。</p>
 */
@RequiredArgsConstructor
public class PortalTenantScopeDirectory {

    private final TenantDao tenantDao;

    /**
     * 按租户标识解析作用域快照。
     *
     * @param tenantId 租户标识
     * @return 租户作用域；租户不存在时为空
     */
    public Optional<TenantScope> findByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }
        TenantEntity tenant = tenantDao.selectById(tenantId.trim());
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

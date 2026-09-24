package com.innospots.nexus.portal.scope;

import java.util.Optional;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.portal.organization.dao.OrganizationUnitDao;
import com.innospots.nexus.portal.organization.domain.entity.OrganizationUnitEntity;
import com.innospots.nexus.portal.organization.domain.enums.OrganizationUnitType;
import com.innospots.nexus.base.domain.scope.TenantScope;

/**
 * Portal 支持的租户作用域目录（基于租户内组织树根节点，不依赖 platform）。
 */
@RequiredArgsConstructor
public class PortalTenantScopeDirectory {

    private final OrganizationUnitDao organizationUnitDao;

    public Optional<TenantScope> findByTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return Optional.empty();
        }
        OrganizationUnitEntity root = organizationUnitDao.selectOne(
                Wrappers.<OrganizationUnitEntity>lambdaQuery()
                        .eq(OrganizationUnitEntity::getTenantId, tenantId)
                        .eq(OrganizationUnitEntity::getUnitType, OrganizationUnitType.COMPANY.name())
                        .last("LIMIT 1"));
        if (root == null) {
            return fallbackScope(tenantId);
        }
        BasicStatus status = toBasicStatus(root.getStatus());
        TenantSnapshot tenantSnapshot = new TenantSnapshot(
                tenantId,
                root.getUnitCode(),
                root.getUnitName(),
                status);
        OrganizationSnapshot organizationSnapshot = new OrganizationSnapshot(
                tenantId,
                root.getUnitCode(),
                root.getUnitName(),
                null,
                null,
                null,
                status);
        return Optional.of(new TenantScope(tenantSnapshot, organizationSnapshot));
    }

    private static Optional<TenantScope> fallbackScope(String tenantId) {
        TenantSnapshot tenantSnapshot = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
        OrganizationSnapshot organizationSnapshot = new OrganizationSnapshot(
                tenantId, tenantId, tenantId, null, null, null, BasicStatus.ENABLED);
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

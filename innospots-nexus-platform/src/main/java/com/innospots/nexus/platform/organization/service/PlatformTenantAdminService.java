package com.innospots.nexus.platform.organization.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.tenant.api.TenantUserResolver;
import com.innospots.nexus.platform.organization.domain.entity.TenantAdminEntity;
import com.innospots.nexus.platform.organization.domain.entity.TenantEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole;
import com.innospots.nexus.platform.organization.domain.request.TenantAdminAddRequest;
import com.innospots.nexus.platform.organization.domain.vo.TenantAdminVo;
import com.innospots.nexus.platform.organization.operator.TenantAdminOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;
import com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode;

/**
 * 租户管理员列表与按邮箱添加。
 */
@RequiredArgsConstructor
public class PlatformTenantAdminService {

    private final TenantOperator tenantOperator;
    private final TenantAdminOperator tenantAdminOperator;
    private final TenantUserResolver tenantUserResolver;

    /**
     * 列出租户全部管理员（无分页）；无记录时回退展示 {@code nx_pl_tenant.owner_tenant_user_id} 为所有者。
     */
    public List<TenantAdminVo> listAdmins(String tenantId) {
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        List<TenantAdminEntity> stored = tenantAdminOperator.listByTenantId(tenantId);
        if (!stored.isEmpty()) {
            return stored.stream().map(this::toVo).toList();
        }
        List<TenantAdminVo> legacy = new ArrayList<>();
        if (tenant.getOwnerTenantUserId() != null && !tenant.getOwnerTenantUserId().isBlank()) {
            legacy.add(new TenantAdminVo(
                    tenant.getOwnerTenantUserId(),
                    tenant.getTenantId(),
                    null,
                    TenantAdminRole.OWNER.name(),
                    tenant.getOwnerTenantUserId(),
                    null));
        }
        return legacy;
    }

    /**
     * 按邮箱添加管理员；若注入了 {@link TenantUserResolver} 则要求邮箱对应用户已存在。
     */
    @Transactional
    public TenantAdminVo addAdminByEmail(String tenantId, TenantAdminAddRequest request) {
        Objects.requireNonNull(request, "request");
        TenantEntity tenant = tenantOperator.requireById(tenantId);
        String email = TenantAdminOperator.requireValidEmail(request.email());
        String adminRole = TenantAdminOperator.requireAdminRole(request.adminRole());

        TenantUserResolver.ResolvedTenantUser resolved = null;
        if (tenantUserResolver != null) {
            resolved = tenantUserResolver.findByEmail(email)
                    .orElseThrow(() -> NexusException.build(PlatformOrganizationStatusCode.TENANT_USER_EMAIL_NOT_FOUND));
        }

        TenantAdminEntity admin = new TenantAdminEntity();
        admin.setTenantId(tenantId);
        admin.setEmail(email);
        admin.setAdminRole(adminRole);
        if (resolved != null) {
            admin.setTenantUserId(resolved.tenantUserId());
            admin.setDisplayName(resolved.displayName());
        }
        tenantAdminOperator.insert(admin);

        if (TenantAdminRole.OWNER.name().equals(adminRole)
                && (tenant.getOwnerTenantUserId() == null || tenant.getOwnerTenantUserId().isBlank())
                && admin.getTenantUserId() != null
                && !admin.getTenantUserId().isBlank()) {
            tenant.setOwnerTenantUserId(admin.getTenantUserId());
            tenantOperator.update(tenant);
        }
        return toVo(admin);
    }

    private TenantAdminVo toVo(TenantAdminEntity admin) {
        Checks.notNull(admin, "admin");
        return new TenantAdminVo(
                admin.getTenantAdminId(),
                admin.getTenantId(),
                admin.getEmail(),
                admin.getAdminRole(),
                admin.getTenantUserId(),
                admin.getDisplayName());
    }
}

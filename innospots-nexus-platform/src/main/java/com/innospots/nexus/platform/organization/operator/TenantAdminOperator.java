package com.innospots.nexus.platform.organization.operator;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.platform.organization.dao.TenantAdminDao;
import com.innospots.nexus.platform.organization.domain.entity.TenantAdminEntity;
import com.innospots.nexus.platform.organization.domain.enums.TenantAdminRole;
import com.innospots.nexus.platform.organization.status.PlatformOrganizationStatusCode;

/**
 * {@code nx_pl_tenant_admin} 查询与插入。
 */
@RequiredArgsConstructor
public class TenantAdminOperator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final TenantAdminDao tenantAdminDao;

    public List<TenantAdminEntity> listByTenantId(String tenantId) {
        Checks.notBlank(tenantId, "tenantId");
        LambdaQueryWrapper<TenantAdminEntity> query = new LambdaQueryWrapper<>();
        query.eq(TenantAdminEntity::getTenantId, tenantId)
                .orderByAsc(TenantAdminEntity::getCreatedAt);
        return tenantAdminDao.selectList(query);
    }

    public TenantAdminEntity requireByTenantIdAndAdminId(String tenantId, String tenantAdminId) {
        Checks.notBlank(tenantId, "tenantId");
        Checks.notBlank(tenantAdminId, "tenantAdminId");
        LambdaQueryWrapper<TenantAdminEntity> query = new LambdaQueryWrapper<>();
        query.eq(TenantAdminEntity::getTenantId, tenantId)
                .eq(TenantAdminEntity::getTenantAdminId, tenantAdminId);
        TenantAdminEntity admin = tenantAdminDao.selectOne(query);
        if (admin == null) {
            throw NexusException.build(PlatformOrganizationStatusCode.TENANT_ADMIN_NOT_FOUND);
        }
        return admin;
    }

    @Transactional
    public void deleteByTenantIdAndAdminId(String tenantId, String tenantAdminId) {
        TenantAdminEntity admin = requireByTenantIdAndAdminId(tenantId, tenantAdminId);
        tenantAdminDao.deleteById(admin.getTenantAdminId());
    }

    public Optional<TenantAdminEntity> findByTenantIdAndEmail(String tenantId, String email) {
        if (tenantId == null || tenantId.isBlank() || email == null || email.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<TenantAdminEntity> query = new LambdaQueryWrapper<>();
        query.eq(TenantAdminEntity::getTenantId, tenantId)
                .eq(TenantAdminEntity::getEmail, normalizeEmail(email));
        return Optional.ofNullable(tenantAdminDao.selectOne(query));
    }

    @Transactional
    public void insert(TenantAdminEntity admin) {
        Checks.notNull(admin, "admin");
        Checks.notBlank(admin.getTenantId(), "tenantId");
        String email = requireValidEmail(admin.getEmail());
        admin.setEmail(email);
        admin.setAdminRole(requireAdminRole(admin.getAdminRole()));
        if (findByTenantIdAndEmail(admin.getTenantId(), email).isPresent()) {
            throw NexusException.build(PlatformOrganizationStatusCode.TENANT_ADMIN_EMAIL_DUPLICATED);
        }
        tenantAdminDao.insert(admin);
    }

    public static String requireValidEmail(String raw) {
        Checks.notBlank(raw, "email");
        String normalized = normalizeEmail(raw);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw NexusException.build(PlatformOrganizationStatusCode.TENANT_ADMIN_EMAIL_INVALID);
        }
        return normalized;
    }

    public static String requireAdminRole(String raw) {
        Checks.notBlank(raw, "adminRole");
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        try {
            TenantAdminRole.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw NexusException.build(PlatformOrganizationStatusCode.TENANT_ADMIN_ROLE_INVALID);
        }
        return normalized;
    }

    public static String normalizeEmail(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().toLowerCase(Locale.ROOT);
    }
}

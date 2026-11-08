package com.innospots.nexus.console.user.admin.operator;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPageRequest;
import com.innospots.nexus.console.user.admin.status.AdminUserStatusCode;

/**
 * {@code nx_admin_user} 单表读写与生命周期字段变更。
 */
@Slf4j
@RequiredArgsConstructor
public class AdminUserOperator {

    private final AdminUserDao platformUserDao;

    @Transactional
    public void insert(AdminUserEntity entity) {
        platformUserDao.insert(entity);
    }

    @Transactional
    public void update(AdminUserEntity entity) {
        platformUserDao.updateById(entity);
    }

    public Optional<AdminUserEntity> findById(String platformUserId) {
        if (platformUserId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(platformUserDao.selectById(platformUserId));
    }

    /**
     * 按平台用户 ID 批量加载（顺序不保证与入参一致）。
     */
    public Map<String, AdminUserEntity> findByIds(Collection<String> platformUserIds) {
        if (platformUserIds == null || platformUserIds.isEmpty()) {
            return Map.of();
        }
        List<String> ids = platformUserIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<AdminUserEntity> rows = platformUserDao.selectBatchIds(ids);
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AdminUserEntity::getAdminUserId, Function.identity(), (a, b) -> a));
    }

    /**
     * 按登录名、显示名或邮箱模糊匹配，返回有限数量的用户 ID（用于角色成员筛选）。
     */
    public List<String> findPlatformUserIdsByKeyword(String keyword, int limit) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        int capped = Math.min(Math.max(limit, 1), 500);
        String trimmed = keyword.trim();
        LambdaQueryWrapper<AdminUserEntity> query = new LambdaQueryWrapper<>();
        query.select(AdminUserEntity::getAdminUserId);
        query.and(wrapper -> wrapper
                .like(AdminUserEntity::getLoginName, trimmed)
                .or()
                .like(AdminUserEntity::getDisplayName, trimmed)
                .or()
                .like(AdminUserEntity::getEmail, trimmed));
        query.last("LIMIT " + capped);
        List<AdminUserEntity> rows = platformUserDao.selectList(query);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(AdminUserEntity::getAdminUserId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    public AdminUserEntity requireById(String platformUserId) {
        Checks.notBlank(platformUserId, "platformUserId");
        AdminUserEntity entity = platformUserDao.selectById(platformUserId);
        if (entity == null) {
            throw NexusException.build(AdminUserStatusCode.PLATFORM_USER_NOT_FOUND);
        }
        return entity;
    }

    public PageResult<AdminUserEntity> page(AdminUserPageRequest request) {
        AdminUserPageRequest pageRequest = request == null ? new AdminUserPageRequest() : request;
        LambdaQueryWrapper<AdminUserEntity> query = new LambdaQueryWrapper<>();
        if (pageRequest.status() != null) {
            query.eq(AdminUserEntity::getStatus, pageRequest.status().name());
        }
        String input = pageRequest.input();
        if (input != null && !input.isBlank()) {
            String keyword = input.trim();
            query.and(wrapper -> wrapper
                    .like(AdminUserEntity::getLoginName, keyword)
                    .or()
                    .like(AdminUserEntity::getDisplayName, keyword));
        }
        query.orderByDesc(AdminUserEntity::getCreatedAt);
        IPage<AdminUserEntity> selectedPage = platformUserDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<AdminUserEntity> records = selectedPage.getRecords();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public void ensureLoginNameAvailable(String loginName) {
        Checks.notBlank(loginName, "loginName");
        if (existsLoginName(loginName.trim())) {
            throw NexusException.build(AdminUserStatusCode.LOGIN_NAME_DUPLICATED);
        }
    }

    public void ensureEmailAvailable(String email, String excludePlatformUserId) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.trim();
        LambdaQueryWrapper<AdminUserEntity> query = new LambdaQueryWrapper<AdminUserEntity>()
                .apply("LOWER(email) = {0}", normalized.toLowerCase());
        if (excludePlatformUserId != null && !excludePlatformUserId.isBlank()) {
            query.ne(AdminUserEntity::getAdminUserId, excludePlatformUserId);
        }
        if (platformUserDao.selectCount(query) > 0) {
            throw NexusException.build(AdminUserStatusCode.EMAIL_DUPLICATED);
        }
    }

    public void ensureMobileAvailable(String mobile, String excludePlatformUserId) {
        if (mobile == null || mobile.isBlank()) {
            return;
        }
        String normalized = normalizeMobile(mobile);
        LambdaQueryWrapper<AdminUserEntity> query = new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getMobile, normalized);
        if (excludePlatformUserId != null && !excludePlatformUserId.isBlank()) {
            query.ne(AdminUserEntity::getAdminUserId, excludePlatformUserId);
        }
        if (platformUserDao.selectCount(query) > 0) {
            throw NexusException.build(AdminUserStatusCode.MOBILE_DUPLICATED);
        }
    }

    public AdminUserEntity newEntity(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String employeeNo,
            AdminUserStatus status,
            String securityRealm
    ) {
        AdminUserEntity user = new AdminUserEntity();
        user.setLoginName(loginName);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setEmployeeNo(employeeNo);
        user.setStatus(status.name());
        user.setSecurityRealm(normalizeRealm(securityRealm));
        return user;
    }

    /**
     * 归一化安全域代码，缺省或非法值回退 {@link SecurityRealm#PLATFORM}。
     *
     * @param securityRealm 安全域代码
     * @return 规范化的安全域代码
     */
    private static String normalizeRealm(String securityRealm) {
        if (securityRealm == null || securityRealm.isBlank()) {
            return SecurityRealm.PLATFORM.name();
        }
        for (SecurityRealm candidate : SecurityRealm.values()) {
            if (candidate.name().equalsIgnoreCase(securityRealm.trim())) {
                return candidate.name();
            }
        }
        return SecurityRealm.PLATFORM.name();
    }

    @Transactional
    public void updateStatus(AdminUserEntity entity, AdminUserStatus status) {
        entity.setStatus(status.name());
        platformUserDao.updateById(entity);
        log.info("Updated platform user {} status to {}", entity.getAdminUserId(), status);
    }

    @Transactional
    public void activatePendingApproval(AdminUserEntity entity) {
        if (!AdminUserStatus.PENDING_APPROVAL.name().equals(entity.getStatus())) {
            throw NexusException.build(AdminUserStatusCode.PLATFORM_USER_NOT_PENDING_APPROVAL);
        }
        entity.setStatus(AdminUserStatus.ACTIVE.name());
        platformUserDao.updateById(entity);
        log.info("Activated platform user {} from registration approval", entity.getAdminUserId());
    }

    @Transactional
    public void markDisabled(AdminUserEntity entity) {
        entity.setStatus(AdminUserStatus.DISABLED.name());
        platformUserDao.updateById(entity);
        log.info("Disabled platform user {}", entity.getAdminUserId());
    }

    private boolean existsLoginName(String loginName) {
        return platformUserDao.selectCount(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getLoginName, loginName)) > 0;
    }

    private static String normalizeMobile(String mobile) {
        return mobile.trim().replace(" ", "");
    }
}

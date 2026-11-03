package com.innospots.nexus.platform.user.operator;

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
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserPageRequest;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;

/**
 * {@code nx_pl_user} 单表读写与生命周期字段变更。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformUserOperator {

    private final PlatformUserDao platformUserDao;

    @Transactional
    public void insert(PlatformUserEntity entity) {
        platformUserDao.insert(entity);
    }

    @Transactional
    public void update(PlatformUserEntity entity) {
        platformUserDao.updateById(entity);
    }

    public Optional<PlatformUserEntity> findById(String platformUserId) {
        if (platformUserId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(platformUserDao.selectById(platformUserId));
    }

    /**
     * 按平台用户 ID 批量加载（顺序不保证与入参一致）。
     */
    public Map<String, PlatformUserEntity> findByIds(Collection<String> platformUserIds) {
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
        List<PlatformUserEntity> rows = platformUserDao.selectBatchIds(ids);
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        return rows.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(PlatformUserEntity::getPlatformUserId, Function.identity(), (a, b) -> a));
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
        LambdaQueryWrapper<PlatformUserEntity> query = new LambdaQueryWrapper<>();
        query.select(PlatformUserEntity::getPlatformUserId);
        query.and(wrapper -> wrapper
                .like(PlatformUserEntity::getLoginName, trimmed)
                .or()
                .like(PlatformUserEntity::getDisplayName, trimmed)
                .or()
                .like(PlatformUserEntity::getEmail, trimmed));
        query.last("LIMIT " + capped);
        List<PlatformUserEntity> rows = platformUserDao.selectList(query);
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream()
                .map(PlatformUserEntity::getPlatformUserId)
                .filter(id -> id != null && !id.isBlank())
                .toList();
    }

    public PlatformUserEntity requireById(String platformUserId) {
        Checks.notBlank(platformUserId, "platformUserId");
        PlatformUserEntity entity = platformUserDao.selectById(platformUserId);
        if (entity == null) {
            throw NexusException.build(PlatformUserStatusCode.PLATFORM_USER_NOT_FOUND);
        }
        return entity;
    }

    public PageResult<PlatformUserEntity> page(PlatformUserPageRequest request) {
        PlatformUserPageRequest pageRequest = request == null ? new PlatformUserPageRequest() : request;
        LambdaQueryWrapper<PlatformUserEntity> query = new LambdaQueryWrapper<>();
        if (pageRequest.status() != null) {
            query.eq(PlatformUserEntity::getStatus, pageRequest.status().name());
        }
        String input = pageRequest.input();
        if (input != null && !input.isBlank()) {
            String keyword = input.trim();
            query.and(wrapper -> wrapper
                    .like(PlatformUserEntity::getLoginName, keyword)
                    .or()
                    .like(PlatformUserEntity::getDisplayName, keyword));
        }
        query.orderByDesc(PlatformUserEntity::getCreatedAt);
        IPage<PlatformUserEntity> selectedPage = platformUserDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                query);
        List<PlatformUserEntity> records = selectedPage.getRecords();
        return PageResult.of(records, pageRequest.pageNo(), pageRequest.pageSize(), selectedPage.getTotal());
    }

    public void ensureLoginNameAvailable(String loginName) {
        Checks.notBlank(loginName, "loginName");
        if (existsLoginName(loginName.trim())) {
            throw NexusException.build(PlatformUserStatusCode.LOGIN_NAME_DUPLICATED);
        }
    }

    public void ensureEmailAvailable(String email, String excludePlatformUserId) {
        if (email == null || email.isBlank()) {
            return;
        }
        String normalized = email.trim();
        LambdaQueryWrapper<PlatformUserEntity> query = new LambdaQueryWrapper<PlatformUserEntity>()
                .apply("LOWER(email) = {0}", normalized.toLowerCase());
        if (excludePlatformUserId != null && !excludePlatformUserId.isBlank()) {
            query.ne(PlatformUserEntity::getPlatformUserId, excludePlatformUserId);
        }
        if (platformUserDao.selectCount(query) > 0) {
            throw NexusException.build(PlatformUserStatusCode.EMAIL_DUPLICATED);
        }
    }

    public void ensureMobileAvailable(String mobile, String excludePlatformUserId) {
        if (mobile == null || mobile.isBlank()) {
            return;
        }
        String normalized = normalizeMobile(mobile);
        LambdaQueryWrapper<PlatformUserEntity> query = new LambdaQueryWrapper<PlatformUserEntity>()
                .eq(PlatformUserEntity::getMobile, normalized);
        if (excludePlatformUserId != null && !excludePlatformUserId.isBlank()) {
            query.ne(PlatformUserEntity::getPlatformUserId, excludePlatformUserId);
        }
        if (platformUserDao.selectCount(query) > 0) {
            throw NexusException.build(PlatformUserStatusCode.MOBILE_DUPLICATED);
        }
    }

    public PlatformUserEntity newEntity(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String employeeNo,
            PlatformUserStatus status
    ) {
        PlatformUserEntity user = new PlatformUserEntity();
        user.setLoginName(loginName);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setEmployeeNo(employeeNo);
        user.setStatus(status.name());
        return user;
    }

    @Transactional
    public void updateStatus(PlatformUserEntity entity, PlatformUserStatus status) {
        entity.setStatus(status.name());
        platformUserDao.updateById(entity);
        log.info("Updated platform user {} status to {}", entity.getPlatformUserId(), status);
    }

    @Transactional
    public void activatePendingApproval(PlatformUserEntity entity) {
        if (!PlatformUserStatus.PENDING_APPROVAL.name().equals(entity.getStatus())) {
            throw NexusException.build(PlatformUserStatusCode.PLATFORM_USER_NOT_PENDING_APPROVAL);
        }
        entity.setStatus(PlatformUserStatus.ACTIVE.name());
        platformUserDao.updateById(entity);
        log.info("Activated platform user {} from registration approval", entity.getPlatformUserId());
    }

    @Transactional
    public void markDisabled(PlatformUserEntity entity) {
        entity.setStatus(PlatformUserStatus.DISABLED.name());
        platformUserDao.updateById(entity);
        log.info("Disabled platform user {}", entity.getPlatformUserId());
    }

    private boolean existsLoginName(String loginName) {
        return platformUserDao.selectCount(new LambdaQueryWrapper<PlatformUserEntity>()
                .eq(PlatformUserEntity::getLoginName, loginName)) > 0;
    }

    private static String normalizeMobile(String mobile) {
        return mobile.trim().replace(" ", "");
    }
}

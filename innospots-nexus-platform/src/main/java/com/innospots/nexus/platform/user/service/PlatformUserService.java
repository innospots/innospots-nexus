package com.innospots.nexus.platform.user.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserPageRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserStatusUpdateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserUpdateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.platform.user.operator.PlatformUserOperator;
import com.innospots.nexus.platform.user.status.PlatformUserStatusCode;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

/**
 * 平台用户管理工作流：档案 CRUD、密码登记与自助注册 provisioning。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.user.endpoint.PlatformUserEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformUserService {

    private final PlatformUserOperator platformUserOperator;
    private final CredentialService credentialService;
    private final PasswordDecryptor passwordDecryptor;
    private final PlatformUserRoleProvisioner platformUserRoleProvisioner;

    public PageResult<PlatformUserVo> pageUsers(PlatformUserPageRequest request) {
        PageResult<PlatformUserEntity> page = platformUserOperator.page(request);
        return PageResult.of(
                page.records().stream().map(this::toVo).toList(),
                page.pageNo(),
                page.pageSize(),
                page.total());
    }

    public PlatformUserVo getUser(String platformUserId) {
        return toVo(platformUserOperator.requireById(platformUserId));
    }

    @Transactional
    public PlatformUserVo createUser(PlatformUserCreateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.loginName(), "loginName");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        String loginName = request.loginName().trim();
        platformUserOperator.ensureLoginNameAvailable(loginName);
        platformUserOperator.ensureEmailAvailable(request.email(), null);
        platformUserOperator.ensureMobileAvailable(request.mobile(), null);

        PlatformUserEntity user = platformUserOperator.newEntity(
                loginName,
                request.displayName(),
                request.email(),
                normalizeMobileForStorage(request.mobile()),
                request.employeeNo(),
                PlatformUserStatus.ACTIVE);
        platformUserOperator.insert(user);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        enrollPassword(user.getPlatformUserId(), rawPassword);
        platformUserRoleProvisioner.assignDefaultRolesIfPresent(user.getPlatformUserId(), request.roleCodes());
        log.info("Created platform user {}", user.getPlatformUserId());
        return toVo(user);
    }

    @Transactional
    public PlatformUserVo createActiveUserWithPassword(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String rawPassword
    ) {
        return createUserWithPassword(loginName, displayName, email, mobile, null, rawPassword, PlatformUserStatus.ACTIVE);
    }

    @Transactional
    public PlatformUserVo createActiveUserWithPassword(
            String loginName,
            String email,
            String mobile,
            String rawPassword
    ) {
        return createActiveUserWithPassword(loginName, null, email, mobile, rawPassword);
    }

    @Transactional
    public PlatformUserVo createPendingApprovalUserWithPassword(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String rawPassword
    ) {
        return createUserWithPassword(
                loginName,
                displayName,
                email,
                mobile,
                null,
                rawPassword,
                PlatformUserStatus.PENDING_APPROVAL);
    }

    @Transactional
    public PlatformUserVo activatePendingApprovalUser(String platformUserId) {
        PlatformUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.activatePendingApproval(entity);
        return toVo(entity);
    }

    @Transactional
    public void disableUserAfterRegistrationRejected(String platformUserId) {
        PlatformUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.markDisabled(entity);
    }

    @Transactional
    public PlatformUserVo updateUser(String platformUserId, PlatformUserUpdateRequest request) {
        Checks.notNull(request, "request");
        PlatformUserEntity entity = platformUserOperator.requireById(platformUserId);
        if (request.displayName() != null) {
            entity.setDisplayName(request.displayName());
        }
        if (request.email() != null) {
            platformUserOperator.ensureEmailAvailable(request.email(), platformUserId);
            entity.setEmail(request.email().trim());
        }
        if (request.mobile() != null) {
            String normalizedMobile = normalizeMobileForStorage(request.mobile());
            platformUserOperator.ensureMobileAvailable(normalizedMobile, platformUserId);
            entity.setMobile(normalizedMobile);
        }
        if (request.employeeNo() != null) {
            entity.setEmployeeNo(request.employeeNo());
        }
        platformUserOperator.update(entity);
        log.info("Updated platform user profile {}", platformUserId);
        return toVo(entity);
    }

    @Transactional
    public void updateUserStatus(String platformUserId, PlatformUserStatusUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.status(), "status");
        if (request.status() != PlatformUserStatus.ACTIVE && request.status() != PlatformUserStatus.DISABLED) {
            throw NexusException.build(PlatformUserStatusCode.STATUS_UPDATE_NOT_ALLOWED);
        }
        PlatformUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.updateStatus(entity, request.status());
    }

    private PlatformUserVo createUserWithPassword(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String employeeNo,
            String rawPassword,
            PlatformUserStatus status
    ) {
        Checks.notBlank(loginName, "loginName");
        Checks.notBlank(rawPassword, "rawPassword");
        String normalizedLogin = loginName.trim();
        platformUserOperator.ensureLoginNameAvailable(normalizedLogin);
        platformUserOperator.ensureEmailAvailable(email, null);
        platformUserOperator.ensureMobileAvailable(mobile, null);

        PlatformUserEntity user = platformUserOperator.newEntity(
                normalizedLogin,
                displayName,
                email,
                normalizeMobileForStorage(mobile),
                employeeNo,
                status);
        platformUserOperator.insert(user);
        enrollPassword(user.getPlatformUserId(), rawPassword);
        log.info("Provisioned platform user {} with status {}", user.getPlatformUserId(), status);
        return toVo(user);
    }

    private void enrollPassword(String platformUserId, String rawPassword) {
        credentialService.enrollPassword(SecurityRealm.PLATFORM, platformUserId, rawPassword);
    }

    private static String normalizeMobileForStorage(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return mobile;
        }
        return mobile.trim().replace(" ", "");
    }

    private PlatformUserVo toVo(PlatformUserEntity entity) {
        return new PlatformUserVo(
                entity.getPlatformUserId(),
                entity.getLoginName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getEmployeeNo(),
                entity.getStatus(),
                entity.getLastLoginTime(),
                entity.getLastLoginIp(),
                entity.getCreatedAt());
    }
}

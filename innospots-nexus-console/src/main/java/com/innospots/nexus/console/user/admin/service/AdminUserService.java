package com.innospots.nexus.console.user.admin.service;

import java.util.Objects;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;
import com.innospots.nexus.console.user.admin.domain.enums.AdminUserStatus;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserCreateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPageRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPasswordResetRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserStatusUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.vo.AdminUserVo;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.user.admin.operator.AdminUserOperator;
import com.innospots.nexus.console.user.admin.status.AdminUserStatusCode;
import com.innospots.nexus.console.support.MutationLog;
import com.innospots.nexus.console.support.RequestTrace;
import com.innospots.nexus.console.user.admin.support.AdminUserRoleProvisioner;

/**
 * 平台用户管理工作流：档案 CRUD、密码登记与自助注册 provisioning。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.console.user.admin.endpoint.AdminUserEndpoint
 */
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {

    private final AdminUserOperator platformUserOperator;
    private final CredentialService credentialService;
    private final PasswordDecryptor passwordDecryptor;
    private final AdminUserRoleProvisioner platformUserRoleProvisioner;

    public PageResult<AdminUserVo> pageUsers(AdminUserPageRequest request) {
        PageResult<AdminUserEntity> page = platformUserOperator.page(request);
        return PageResult.of(
                page.records().stream().map(this::toVo).toList(),
                page.pageNo(),
                page.pageSize(),
                page.total());
    }

    public AdminUserVo getUser(String platformUserId) {
        return toVo(platformUserOperator.requireById(platformUserId));
    }

    @Transactional
    public AdminUserVo createUser(AdminUserCreateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notBlank(request.loginName(), "loginName");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        String loginName = request.loginName().trim();
        platformUserOperator.ensureLoginNameAvailable(loginName);
        platformUserOperator.ensureEmailAvailable(request.email(), null);
        platformUserOperator.ensureMobileAvailable(request.mobile(), null);

        AdminUserEntity user = platformUserOperator.newEntity(
                loginName,
                request.displayName(),
                request.email(),
                normalizeMobileForStorage(request.mobile()),
                null,
                AdminUserStatus.ACTIVE);
        platformUserOperator.insert(user);

        String rawPassword = decryptEncryptedPassword(request.encryptedPassword(), user.getPlatformUserId());
        enrollPassword(user.getPlatformUserId(), rawPassword);
        platformUserRoleProvisioner.assignDefaultRolesIfPresent(user.getPlatformUserId(), request.roleCodes());
        MutationLog.info(
                log,
                "platform user created platformUserId={} login={}",
                user.getPlatformUserId(),
                RequestTrace.safeLogin(user.getLoginName()));
        return toVo(user);
    }

    @Transactional
    public AdminUserVo createActiveUserWithPassword(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String rawPassword
    ) {
        return createUserWithPassword(loginName, displayName, email, mobile, null, rawPassword, AdminUserStatus.ACTIVE);
    }

    @Transactional
    public AdminUserVo createActiveUserWithPassword(
            String loginName,
            String email,
            String mobile,
            String rawPassword
    ) {
        return createActiveUserWithPassword(loginName, null, email, mobile, rawPassword);
    }

    @Transactional
    public AdminUserVo createPendingApprovalUserWithPassword(
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
                AdminUserStatus.PENDING_APPROVAL);
    }

    @Transactional
    public AdminUserVo activatePendingApprovalUser(String platformUserId) {
        AdminUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.activatePendingApproval(entity);
        MutationLog.info(log, "platform user activated platformUserId={}", platformUserId);
        return toVo(entity);
    }

    @Transactional
    public void disableUserAfterRegistrationRejected(String platformUserId) {
        AdminUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.markDisabled(entity);
        MutationLog.info(
                log,
                "platform user disabled after registration rejected platformUserId={}",
                platformUserId);
    }

    @Transactional
    public AdminUserVo updateUser(String platformUserId, AdminUserUpdateRequest request) {
        Checks.notNull(request, "request");
        AdminUserEntity entity = platformUserOperator.requireById(platformUserId);
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
        platformUserOperator.update(entity);
        MutationLog.info(log, "platform user profile updated platformUserId={}", platformUserId);
        return toVo(entity);
    }

    /**
     * 管理员重置指定用户的登录密码（无需旧密码）。
     */
    @Transactional
    public void resetUserPassword(String platformUserId, AdminUserPasswordResetRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        platformUserOperator.requireById(platformUserId);
        String rawPassword = decryptEncryptedPassword(request.encryptedPassword(), platformUserId);
        try {
            credentialService.resetPassword(SecurityRealm.PLATFORM, platformUserId, rawPassword);
        } catch (IllegalStateException ex) {
            credentialService.enrollPassword(SecurityRealm.PLATFORM, platformUserId, rawPassword);
        }
        MutationLog.info(log, "platform user password reset by admin platformUserId={}", platformUserId);
    }

    @Transactional
    public void updateUserStatus(String platformUserId, AdminUserStatusUpdateRequest request) {
        Objects.requireNonNull(request, "request");
        Checks.notNull(request.status(), "status");
        if (request.status() != AdminUserStatus.ACTIVE && request.status() != AdminUserStatus.DISABLED) {
            throw NexusException.build(AdminUserStatusCode.STATUS_UPDATE_NOT_ALLOWED);
        }
        AdminUserEntity entity = platformUserOperator.requireById(platformUserId);
        platformUserOperator.updateStatus(entity, request.status());
        MutationLog.info(
                log,
                "platform user status updated platformUserId={} status={}",
                platformUserId,
                request.status());
    }

    private AdminUserVo createUserWithPassword(
            String loginName,
            String displayName,
            String email,
            String mobile,
            String employeeNo,
            String rawPassword,
            AdminUserStatus status
    ) {
        Checks.notBlank(loginName, "loginName");
        Checks.notBlank(rawPassword, "rawPassword");
        String normalizedLogin = loginName.trim();
        platformUserOperator.ensureLoginNameAvailable(normalizedLogin);
        platformUserOperator.ensureEmailAvailable(email, null);
        platformUserOperator.ensureMobileAvailable(mobile, null);

        AdminUserEntity user = platformUserOperator.newEntity(
                normalizedLogin,
                displayName,
                email,
                normalizeMobileForStorage(mobile),
                employeeNo,
                status);
        platformUserOperator.insert(user);
        enrollPassword(user.getPlatformUserId(), rawPassword);
        MutationLog.info(
                log,
                "platform user provisioned platformUserId={} login={} status={}",
                user.getPlatformUserId(),
                RequestTrace.safeLogin(user.getLoginName()),
                status);
        return toVo(user);
    }

    private void enrollPassword(String platformUserId, String rawPassword) {
        credentialService.enrollPassword(SecurityRealm.PLATFORM, platformUserId, rawPassword);
    }

    private String decryptEncryptedPassword(String encryptedPassword, String platformUserId) {
        try {
            String rawPassword = passwordDecryptor.decrypt(encryptedPassword);
            log.debug(
                    "Platform user password decrypted: platformUserId={}, plaintextLength={}",
                    platformUserId,
                    rawPassword.length());
            return rawPassword;
        } catch (NexusException ex) {
            log.warn(
                    "Platform user password decrypt failed: platformUserId={}, encryptedLength={}, code={}",
                    platformUserId,
                    encryptedPassword.length(),
                    ex.code());
            throw ex;
        }
    }

    private static String normalizeMobileForStorage(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return mobile;
        }
        return mobile.trim().replace(" ", "");
    }

    private AdminUserVo toVo(AdminUserEntity entity) {
        return new AdminUserVo(
                entity.getPlatformUserId(),
                entity.getLoginName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getStatus(),
                entity.getLastLoginTime(),
                entity.getLastLoginIp(),
                entity.getCreatedAt());
    }
}

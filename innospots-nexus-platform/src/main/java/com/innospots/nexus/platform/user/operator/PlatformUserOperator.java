package com.innospots.nexus.platform.user.operator;

import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.platform.user.dao.PlatformUserDao;
import com.innospots.nexus.platform.user.domain.entity.PlatformUserEntity;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

/**
 * 持久化平台用户及其本地密码凭证。
 * <p>无公开自助注册路径。管理员通过本 Operator 创建账号。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformUserOperator {

    private final PlatformUserDao platformUserDao;
    private final CredentialService credentialService;
    private final PasswordDecryptor passwordDecryptor;

    /**
     * 按标识符查找平台用户。
     *
     * @param platformUserId platform-realm user 标识符
     * @return 找到时返回用户概要
     */
    public Optional<PlatformUserVo> findById(String platformUserId) {
        if (platformUserId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(platformUserDao.selectById(platformUserId)).map(this::toVo);
    }

    /**
     * 使用本地密码创建平台用户。不签发令牌。
     *
     * @param request admin create 请求
     * @return created user 概要
     */
    @Transactional
    public PlatformUserVo createWithPassword(PlatformUserCreateRequest request) {
        requireText(request == null ? null : request.loginName(), "loginName");
        requireText(request == null ? null : request.encryptedPassword(), "encryptedPassword");

        PlatformUserEntity user = new PlatformUserEntity();
        user.setLoginName(request.loginName());
        user.setDisplayName(request.displayName());
        user.setEmail(request.email());
        user.setMobile(request.mobile());
        user.setEmployeeNo(request.employeeNo());
        user.setStatus(PlatformUserStatus.ACTIVE.name());
        platformUserDao.insert(user);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        credentialService.enrollPassword(SecurityRealm.PLATFORM, user.getPlatformUserId(), rawPassword);

        log.info("Created platform user {}", user.getPlatformUserId());
        return toVo(user);
    }

    private PlatformUserVo toVo(PlatformUserEntity entity) {
        return new PlatformUserVo(
                entity.getPlatformUserId(),
                entity.getLoginName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getEmployeeNo(),
                entity.getStatus()
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw NexusException.build(
                    NexusStatusCode.INVALID_PARAMETER.fullCode(),
                    fieldName + " is required");
        }
    }
}

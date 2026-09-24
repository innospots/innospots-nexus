package com.innospots.nexus.portal.user.operator;

import java.util.Objects;
import java.util.Optional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.domain.data.DataPage;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.portal.user.dao.UserDao;
import com.innospots.nexus.portal.user.domain.entity.UserEntity;
import com.innospots.nexus.portal.user.domain.enums.UserRegisterSource;
import com.innospots.nexus.portal.user.domain.enums.UserStatus;
import com.innospots.nexus.portal.user.domain.request.UserPageRequest;
import com.innospots.nexus.portal.user.domain.request.UserPasswordRegisterRequest;
import com.innospots.nexus.portal.user.domain.vo.UserProfileVo;

/**
 * 基于 MyBatis-Plus DAO 的租户域用户数据操作器。
 * <p>注册仅创建登录身份。租户成员关系由
 * {@code nx_tenant_member} 存储且不在此处创建。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Slf4j
@RequiredArgsConstructor
public class UserOperator {

    private final UserDao userDao;
    private final CredentialService credentialService;
    private final PasswordDecryptor passwordDecryptor;

    /**
     * 按标识符查找用户档案。
     *
     * @param userId tenant-realm user 标识符
     * @return user 找到时返回的档案
     */
    public Optional<UserProfileVo> findById(String userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userDao.selectById(userId)).map(this::toProfile);
    }

    /**
     * 使用可选模糊过滤器分页查询用户档案。
     *
     * @param request user page 请求
     * @return user 档案页面
     */
    public DataPage<UserProfileVo> pageUsers(UserPageRequest request) {
        UserPageRequest pageRequest = request == null ? new UserPageRequest() : request;
        IPage<UserEntity> selectedPage = userDao.selectPage(
                new Page<>(pageRequest.pageNo(), pageRequest.pageSize()),
                pageQuery(pageRequest)
        );
        return DataPage.of(
                selectedPage.getRecords().stream().map(this::toProfile).toList(),
                pageRequest.pageNo(),
                pageRequest.pageSize(),
                selectedPage.getTotal()
        );
    }

    /**
     * 按标识符删除用户。
     *
     * @param userId tenant-realm user 标识符
     * @return true 记录删除时间
     */
    @Transactional
    public boolean deleteUser(String userId) {
        if (userId == null) {
            return false;
        }
        return userDao.deleteById(userId) > 0;
    }

    /**
     * 通过禁用生命周期状态冻结用户。
     *
     * @param userId tenant-realm user 标识符
     * @return true 记录更新时间
     */
    @Transactional
    public boolean freezeUser(String userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = new UserEntity();
        user.setTenantUserId(userId);
        user.setStatus(UserStatus.DISABLED.name());
        return userDao.updateById(user) > 0;
    }

    /**
     * 通过恢复生命周期状态解冻用户。
     *
     * @param userId tenant-realm user 标识符
     * @return true 记录更新时间
     */
    @Transactional
    public boolean unfreezeUser(String userId) {
        if (userId == null) {
            return false;
        }
        UserEntity user = new UserEntity();
        user.setTenantUserId(userId);
        user.setStatus(UserStatus.ACTIVE.name());
        return userDao.updateById(user) > 0;
    }

    /**
     * 使用本地密码凭证注册租户域身份。
     * 不创建租户成员关系。
     *
     * @param request registration 请求 with 前端加密密码
     * @return created 用户档案
     */
    @Transactional
    public UserProfileVo registerWithPassword(UserPasswordRegisterRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        UserEntity user = createUserEntity(
                request.userName(),
                request.displayName(),
                request.email(),
                request.mobile(),
                request.region(),
                request.timeZone(),
                request.language(),
                UserRegisterSource.PASSWORD);
        userDao.insert(user);

        String rawPassword = passwordDecryptor.decrypt(request.encryptedPassword());
        credentialService.enrollPassword(SecurityRealm.TENANT, user.getTenantUserId(), rawPassword);

        return toProfile(user);
    }

    private UserEntity createUserEntity(
            String userName,
            String displayName,
            String email,
            String mobile,
            String region,
            String timeZone,
            String language,
            UserRegisterSource registerSource
    ) {
        UserEntity user = new UserEntity();
        user.setUserName(userName);
        user.setDisplayName(displayName);
        user.setEmail(email);
        user.setMobile(mobile);
        user.setRegion(region);
        user.setTimeZone(timeZone);
        user.setLanguage(language);
        user.setRegisterSource(registerSource.name());
        user.setStatus(UserStatus.ACTIVE.name());
        user.setEmailVerified(false);
        user.setMobileVerified(false);
        return user;
    }

    private QueryWrapper<UserEntity> pageQuery(UserPageRequest request) {
        QueryWrapper<UserEntity> query = new QueryWrapper<>();
        if (hasText(request.input())) {
            query.and(wrapper -> wrapper
                    .like("user_name", request.input())
                    .or()
                    .like("display_name", request.input())
                    .or()
                    .like("email", request.input())
                    .or()
                    .like("mobile", request.input()));
        }
        if (hasText(request.userName())) {
            query.like("user_name", request.userName());
        }
        if (hasText(request.displayName())) {
            query.like("display_name", request.displayName());
        }
        if (hasText(request.email())) {
            query.like("email", request.email());
        }
        if (hasText(request.mobile())) {
            query.like("mobile", request.mobile());
        }
        return query;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private UserProfileVo toProfile(UserEntity entity) {
        return new UserProfileVo(
                entity.getTenantUserId(),
                entity.getUserName(),
                entity.getDisplayName(),
                entity.getEmail(),
                entity.getMobile(),
                entity.getRegion(),
                entity.getTimeZone(),
                entity.getLanguage(),
                entity.getAvatarKey(),
                parseRegisterSource(entity.getRegisterSource()),
                parseStatus(entity.getStatus())
        );
    }

    private UserRegisterSource parseRegisterSource(String value) {
        return value == null ? null : UserRegisterSource.valueOf(value);
    }

    private UserStatus parseStatus(String value) {
        return value == null ? null : UserStatus.valueOf(value);
    }
}

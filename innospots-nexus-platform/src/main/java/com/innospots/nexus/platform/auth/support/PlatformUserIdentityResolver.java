package com.innospots.nexus.platform.auth.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.console.user.admin.dao.AdminUserDao;
import com.innospots.nexus.console.user.admin.domain.entity.AdminUserEntity;

/**
 * 按 loginName / email / mobile 解析平台用户（登录名优先，其次邮箱，再次手机号）。
 *
 * @author Smars
 * @date 2026/10/06
 */
@RequiredArgsConstructor
public class PlatformUserIdentityResolver {

    private final AdminUserDao platformUserDao;

    /**
     * 按身份标识查找用户；不做状态过滤。
     *
     * @param identity loginName、email 或 mobile
     * @return 匹配用户；无匹配或空白 identity 时 {@code null}
     */
    public AdminUserEntity findByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            return null;
        }
        AdminUserEntity byLogin = platformUserDao.selectOne(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getLoginName, identity));
        if (byLogin != null) {
            return byLogin;
        }
        AdminUserEntity byEmail = platformUserDao.selectOne(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getEmail, identity));
        if (byEmail != null) {
            return byEmail;
        }
        return platformUserDao.selectOne(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getMobile, identity));
    }
}

package com.innospots.nexus.console.auth.api;

import java.util.Optional;

import com.innospots.nexus.console.auth.domain.model.AuthUser;

/**
 * 查找登录身份。由 platform 与 portal 各实现一份（构造时即绑定安全域）；console 不持久化用户。
 *
 * @author Smars
 * @date 2026/09/13
 */
public interface UserDirectory {

    /**
     * 按 user_name、email 或 mobile 查找仍可用于认证的用户。
     *
     * @param identity login 标识符
     * @return 匹配的活跃用户
     */
    Optional<AuthUser> findByLogin(String identity);

    /**
     * 按域用户主键加载仍可用于认证的用户（通常为 {@code ACTIVE}）。
     *
     * @param userId platform_user_id 或 tenant_user_id
     * @return 找到且仍有效时返回
     */
    Optional<AuthUser> findById(String userId);
}

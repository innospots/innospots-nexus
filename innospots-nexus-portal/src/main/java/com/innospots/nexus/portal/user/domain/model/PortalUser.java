package com.innospots.nexus.portal.user.domain.model;

import com.innospots.nexus.portal.user.domain.enums.UserStatus;

/**
 * 身份管理的 Portal 用户聚合根。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userId      user 标识符
 * @param account     登录账号
 * @param displayName 显示名称
 * @param email       邮箱地址
 * @param status      生命周期状态
 */
public record PortalUser(
        String userId,
        String account,
        String displayName,
        String email,
        UserStatus status
) {
}

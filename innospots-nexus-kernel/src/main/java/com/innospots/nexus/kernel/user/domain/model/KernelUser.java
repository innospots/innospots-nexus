package com.innospots.nexus.kernel.user.domain.model;

import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * 身份管理的 Kernel 用户聚合根。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userId      user 标识符
 * @param account     登录账号
 * @param displayName 显示名称
 * @param email       邮箱地址
 * @param status      生命周期状态
 */
public record KernelUser(
        String userId,
        String account,
        String displayName,
        String email,
        UserStatus status
) {
}

package com.innospots.nexus.kernel.user.domain.vo;

import com.innospots.nexus.kernel.user.domain.enums.UserRegisterSource;
import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * 租户域用户档案数据的读模型。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userId         tenant-realm user 标识符
 * @param userName       唯一登录用户名
 * @param displayName    界面展示的显示名称
 * @param email          邮箱地址
 * @param mobile         手机号
 * @param region         地区偏好
 * @param timeZone       IANA 时区
 * @param language       界面语言
 * @param avatarKey      头像存储键
 * @param registerSource 原始注册来源
 * @param status         user 生命周期状态
 */
public record UserProfileVo(
        String userId,
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String avatarKey,
        UserRegisterSource registerSource,
        UserStatus status
) {
}

package com.innospots.nexus.console.auth.domain.model;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 用户目录端口返回的域中立用户身份。
 *
 * @author Smars
 * @date 2026/09/13
 * @param userId    platform_user_id 或 tenant_user_id
 * @param loginName 匹配到的登录名、邮箱或手机号
 * @param status    生命周期状态 name
 * @param realm     所属安全域
 */
public record AuthUser(String userId, String loginName, String status, SecurityRealm realm) {
}

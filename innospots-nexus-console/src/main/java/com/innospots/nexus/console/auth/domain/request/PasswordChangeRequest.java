package com.innospots.nexus.console.auth.domain.request;

/**
 * 已认证用户的密码修改。
 *
 * @author Smars
 * @date 2026/09/13
 * @param oldEncryptedPassword 当前前端加密密码
 * @param newEncryptedPassword 期望的前端加密密码
 */
public record PasswordChangeRequest(String oldEncryptedPassword, String newEncryptedPassword) {
}

package com.innospots.nexus.console.auth.domain.request;

/**
 * 刷新令牌交换。
 *
 * @author Smars
 * @date 2026/09/13
 * @param refreshToken 同域签发的刷新令牌
 */
public record TokenRefreshRequest(String refreshToken) {
}

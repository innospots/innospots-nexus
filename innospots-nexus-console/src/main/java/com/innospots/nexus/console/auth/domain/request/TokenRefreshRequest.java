package com.innospots.nexus.console.auth.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 刷新令牌交换。
 *
 * @author Smars
 * @date 2026/09/13
 * @param refreshToken 同域签发的刷新令牌
 */
@Schema(name = "TokenRefreshRequest", description = "刷新令牌请求")
public record TokenRefreshRequest(
        @Schema(description = "刷新令牌", required = true)
        String refreshToken
) {
}

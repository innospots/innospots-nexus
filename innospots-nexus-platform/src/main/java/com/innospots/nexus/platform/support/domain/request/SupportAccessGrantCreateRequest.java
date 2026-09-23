package com.innospots.nexus.platform.support.domain.request;

import java.time.LocalDateTime;

/**
 * 创建待审批支持访问授权的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId       待访问的租户
 * @param platformUserId 获得访问权限的平台用户
 * @param reason         业务原因
 * @param expireAt       绝对过期时间
 */
public record SupportAccessGrantCreateRequest(
        String tenantId,
        String platformUserId,
        String reason,
        LocalDateTime expireAt
) {
}

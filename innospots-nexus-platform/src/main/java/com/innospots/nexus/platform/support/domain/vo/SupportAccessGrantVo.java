package com.innospots.nexus.platform.support.domain.vo;

import java.time.LocalDateTime;

/**
 * 支持访问授权概要。
 *
 * @author Smars
 * @date 2026/09/13
 * @param grantId        grant 标识符
 * @param tenantId       被访问的租户
 * @param platformUserId 获得访问权限的平台用户
 * @param reason         业务原因
 * @param approvedBy     租户管理员审批人（如有）
 * @param expireAt       绝对过期时间
 * @param status         生命周期状态 name
 */
public record SupportAccessGrantVo(
        String grantId,
        String tenantId,
        String platformUserId,
        String reason,
        String approvedBy,
        LocalDateTime expireAt,
        String status
) {
}

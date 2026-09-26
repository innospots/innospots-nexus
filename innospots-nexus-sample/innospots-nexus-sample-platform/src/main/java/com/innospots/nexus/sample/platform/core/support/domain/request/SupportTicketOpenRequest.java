package com.innospots.nexus.sample.platform.core.support.domain.request;

/**
 * 开通支持工单请求。
 *
 * @param tenantId 租户标识
 * @param subject 主题
 * @author Smars
 * @date 2026/09/26
 */
public record SupportTicketOpenRequest(String tenantId, String subject) {
}

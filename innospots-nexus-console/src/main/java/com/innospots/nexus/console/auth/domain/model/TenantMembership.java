package com.innospots.nexus.console.auth.domain.model;

/**
 * 租户域身份认证后使用的活跃租户成员关系。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId       tenant 标识符
 * @param tenantMemberId tenant member 标识符
 */
public record TenantMembership(String tenantId, String tenantMemberId) {
}

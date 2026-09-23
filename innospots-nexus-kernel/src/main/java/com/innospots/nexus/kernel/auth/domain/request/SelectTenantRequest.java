package com.innospots.nexus.kernel.auth.domain.request;

/**
 * 身份认证后选择当前租户。
 *
 * @param tenantId 待激活的租户
 */
public record SelectTenantRequest(String tenantId) {
}

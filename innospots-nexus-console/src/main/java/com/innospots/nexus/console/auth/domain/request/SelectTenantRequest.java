package com.innospots.nexus.console.auth.domain.request;

/**
 * 身份认证后选择当前租户。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId 待激活的租户
 */
public record SelectTenantRequest(String tenantId) {
}

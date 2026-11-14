package com.innospots.nexus.console.scope.tenant.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 更新租户生命周期状态的 REST 请求体。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.TenantEndpoint#updateTenantStatus
 * @param status {@link com.innospots.nexus.console.scope.tenant.domain.enums.TenantStatus} 名称：
 *               {@code ACTIVE}、{@code PENDING}、{@code DISABLED}、{@code DISSOLVED}
 */
@Schema(name = "TenantStatusUpdateRequest", description = "更新租户状态")
public record TenantStatusUpdateRequest(
        @Schema(
                description = "生命周期状态：ACTIVE（启用）、PENDING（待启用）、DISABLED（停用）、DISSOLVED（解散）",
                required = true,
                examples = {"ACTIVE", "PENDING", "DISABLED", "DISSOLVED"})
        String status
) {
}

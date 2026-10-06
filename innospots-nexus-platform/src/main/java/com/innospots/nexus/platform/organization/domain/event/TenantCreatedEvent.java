package com.innospots.nexus.platform.organization.domain.event;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * 租户创建后发布（企业档案可后续单独维护）。
 *
 * @author Smars
 * @date 2026/09/13
 * @see com.innospots.nexus.base.events.DomainEvent
 * @see com.innospots.nexus.platform.organization.service.PlatformTenantService
 * @param tenantId           已持久化的租户标识
 * @param tenantCode         稳定的租户编码
 * @param ownerTenantUserId  租户用户域中的可选初始所有者
 */
public record TenantCreatedEvent(String tenantId, String tenantCode, String ownerTenantUserId)
        implements DomainEvent {
    @Override
    public String eventType() {
        return "platform.organization.created";
    }
}

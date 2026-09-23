package com.innospots.nexus.platform.tenant.domain.event;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * 租户及其企业档案创建后发布。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId           已持久化的租户标识
 * @param tenantCode         稳定的租户编码
 * @param ownerTenantUserId  租户用户域中的可选初始所有者
 */
public record TenantCreatedEvent(String tenantId, String tenantCode, String ownerTenantUserId)
        implements DomainEvent {
    @Override
    public String eventType() {
        return "platform.tenant.created";
    }
}

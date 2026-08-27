package com.innospots.nexus.platform.tenant.domain.event;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * Published after a tenant and its enterprise profile are created.
 *
 * @param tenantId           persisted tenant identifier
 * @param tenantCode         stable tenant code
 * @param ownerTenantUserId  optional initial owner in the tenant-user realm
 */
public record TenantCreatedEvent(String tenantId, String tenantCode, String ownerTenantUserId)
        implements DomainEvent {

    @Override
    public String eventType() {
        return "platform.tenant.created";
    }
}

package com.innospots.nexus.base.resources;

import com.innospots.nexus.base.events.DomainEvent;

/**
 * Domain event published when resource metadata is saved/persisted.
 */
public record ResourceEvent(MetaResource metaResource) implements DomainEvent {

    @Override
    public String eventType() {
        return "resource.meta.saved";
    }
}

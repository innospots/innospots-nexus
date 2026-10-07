package com.innospots.nexus.core.persistence.scope;

/**
 * {@link com.innospots.nexus.core.persistence.entity.OwnershipEntity} 归属三元组。
 */
public record PersistenceOwnership(
        OwnerType ownerType,
        String ownerId,
        String securityRealm
) {

    public String ownerTypeName() {
        return ownerType.name();
    }
}

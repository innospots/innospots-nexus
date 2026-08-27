package com.innospots.nexus.console.role.domain.enums;

/**
 * Layer that owns a role definition.
 */
public enum RoleOwnerType {

    /** Ops-domain platform role. */
    PLATFORM,

    /** Tenant-wide role. */
    TENANT,

    /** Workspace-scoped role. */
    WORKSPACE
}

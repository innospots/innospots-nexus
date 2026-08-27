package com.innospots.nexus.kernel.organization.domain.enums;

/**
 * Internal organization-tree node type. {@code COMPANY} is the tree root,
 * not the platform enterprise legal profile.
 */
public enum OrganizationUnitType {

    /** Internal tree root for a tenant. */
    COMPANY,

    /** Branch or regional node. */
    BRANCH,

    /** Department node. */
    DEPARTMENT,

    /** Team node. */
    TEAM
}

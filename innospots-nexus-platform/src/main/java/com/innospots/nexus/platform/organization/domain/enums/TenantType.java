package com.innospots.nexus.platform.organization.domain.enums;

/**
 * 平台组织的形态分类（持久化于 {@code nx_pl_tenant.tenant_type}）。
 *
 * @author Smars
 * @date 2026/10/04
 * @see com.innospots.nexus.platform.organization.domain.entity.TenantEntity
 */
public enum TenantType {

    /**
     * 企业客户（法定主体、对公签约等场景）。
     */
    ENTERPRISE,

    /**
     * 团队或协作组织（无独立法人主体）。
     */
    TEAM,

    /**
     * 个人租户。
     */
    INDIVIDUAL
}

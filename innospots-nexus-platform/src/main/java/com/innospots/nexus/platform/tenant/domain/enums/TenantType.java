package com.innospots.nexus.platform.tenant.domain.enums;

/**
 * 平台管理租户的组织形态分类。
 *
 * @author Smars
 * @date 2026/10/04
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

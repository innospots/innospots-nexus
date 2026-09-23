package com.innospots.nexus.kernel.organization.domain.enums;

/**
 * 内部组织树节点类型。{@code COMPANY} 为树根，
 * 而非平台企业法定档案。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum OrganizationUnitType {

    /**
     * 租户内部树根节点。
     */
    COMPANY,

    /**
     * 分支或区域节点。
     */
    BRANCH,

    /**
     * 部门节点。
     */
    DEPARTMENT,

    /**
     * 团队节点。
     */
    TEAM
}

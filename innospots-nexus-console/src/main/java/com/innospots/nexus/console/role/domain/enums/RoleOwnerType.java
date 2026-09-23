package com.innospots.nexus.console.role.domain.enums;

/**
 * 拥有角色定义的层级。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum RoleOwnerType {

    /**
     * 运维域平台角色。
     */
    PLATFORM,

    /**
     * 租户级 role。
     */
    TENANT,

    /**
     * 工作区作用域内的 role。
     */
    WORKSPACE
}

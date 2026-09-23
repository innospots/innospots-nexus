package com.innospots.nexus.console.role.domain.enums;

/**
 * 可绑定到角色的主体。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum RoleBindingSubjectType {

    /**
     * 平台用户或租户成员身份。
     */
    USER,

    /**
     * 租户组织单元。
     */
    ORG_UNIT
}

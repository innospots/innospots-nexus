package com.innospots.nexus.console.role.domain.enums;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;

/**
 * 拥有角色定义的层级。
 *
 * @author Smars
 * @date 2026/09/13
 */
public enum RoleOwnerType {

    /**
     * 运营管理平台角色。
     */
    PLATFORM,

    /**
     * 租户级 role。
     */
    TENANT,

    /**
     * 工作区作用域内的 role。
     */
    WORKSPACE;

    /**
     * 返回该归属层级对应的令牌与用户目录安全域。
     *
     * <p>工作区角色与租户角色同属 {@link SecurityRealm#TENANT}。</p>
     *
     * @return 安全域
     */
    public SecurityRealm securityRealm() {
        if (this == PLATFORM) {
            return SecurityRealm.PLATFORM;
        }
        return SecurityRealm.TENANT;
    }
}

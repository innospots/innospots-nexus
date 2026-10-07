package com.innospots.nexus.console.scope;

import com.innospots.nexus.console.role.domain.enums.RoleOwnerType;

/**
 * 控制台行级归属三元组，由会话或业务规则解析。
 *
 * @param ownerType     PLATFORM、TENANT 或 WORKSPACE
 * @param ownerId       归属 ID；PLATFORM 时为 null
 * @param securityRealm PLATFORM 或 TENANT
 */
public record ConsoleOwnership(
        RoleOwnerType ownerType,
        String ownerId,
        String securityRealm
) {

    public String ownerTypeName() {
        return ownerType.name();
    }
}

package com.innospots.nexus.console.permission.authorization;

import java.util.Set;

/** 由应用安全适配器提供的当前鉴权主体。 */
public record AuthorizationSubject(
        /** 当前用户 ID。 */
        String userId,
        /** 当前用户拥有的角色 ID 集合。 */
        Set<String> roleIds,
        /** 当前用户所属的组织单元 ID 集合。 */
        Set<String> orgUnitIds,
        /** 是否为项目管理员。管理员只绕过普通功能授权。 */
        boolean administrator
) {

    /** 复制角色和组织单元集合，避免鉴权判断依赖可变输入。 */
    public AuthorizationSubject {
        roleIds = roleIds == null ? Set.of() : Set.copyOf(roleIds);
        orgUnitIds = orgUnitIds == null ? Set.of() : Set.copyOf(orgUnitIds);
    }
}

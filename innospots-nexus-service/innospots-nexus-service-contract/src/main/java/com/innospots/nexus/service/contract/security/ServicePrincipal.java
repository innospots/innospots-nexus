package com.innospots.nexus.service.contract.security;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * 不可变调用方身份。集合被复制。匿名主体标识为 {@code anonymous}。
 *
 * @param id          主体标识
 * @param type        主体类型
 * @param realm       认证域
 * @param roles       声明角色，非权限绕过
 * @param permissions 声明权限键
 * @param attributes  额外身份属性
 * @author Smars
 * @date 2026/09/13
 * @see PrincipalType
 * @see ServiceScope
 */
public record ServicePrincipal(
        String id,
        PrincipalType type,
        String realm,
        Set<String> roles,
        Set<String> permissions,
        Map<String, String> attributes
) {

    public static final String ANONYMOUS_ID = "anonymous";

    public ServicePrincipal {
        Checks.notNull(type, "type");
        Checks.notBlank(realm, "realm");
        if (type == PrincipalType.ANONYMOUS) {
            Checks.isTrue(ANONYMOUS_ID.equals(id), "anonymous principal id must be anonymous");
        } else {
            Checks.notBlank(id, "id");
        }
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    /**
     * 返回 {@code realm} 的匿名主体。
     *
     * @param realm 认证域
     * @return 匿名主体
     */
    public static ServicePrincipal anonymous(String realm) {
        return new ServicePrincipal(ANONYMOUS_ID, PrincipalType.ANONYMOUS, realm, Set.of(), Set.of(), Map.of());
    }
}

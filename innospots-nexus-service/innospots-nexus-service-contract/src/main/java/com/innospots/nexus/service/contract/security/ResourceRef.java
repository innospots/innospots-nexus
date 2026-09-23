package com.innospots.nexus.service.contract.security;

import com.innospots.nexus.base.util.Checks;

/**
 * 授权与审计使用的资源标识。
 *
 * @param type  资源类型
 * @param id    资源标识
 * @param scope 所属作用域
 * @author Smars
 * @date 2026/09/13
 * @see ServiceScope
 */
public record ResourceRef(String type, String id, ServiceScope scope) {

    public ResourceRef {
        Checks.notBlank(type, "type");
        Checks.notBlank(id, "id");
        Checks.notNull(scope, "scope");
    }
}

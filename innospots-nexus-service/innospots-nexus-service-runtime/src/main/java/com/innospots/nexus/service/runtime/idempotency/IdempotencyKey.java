package com.innospots.nexus.service.runtime.idempotency;

import com.innospots.nexus.base.util.Checks;

/**
 * 幂等键复合标识。二期用于单 JVM 去重。
 *
 * @param realm       认证域
 * @param scopeKey    作用域键
 * @param principalId 主体标识
 * @param operationId 操作标识
 * @param key         客户端幂等键
 * @author Smars
 * @date 2026/09/15
 */
public record IdempotencyKey(
        String realm,
        String scopeKey,
        String principalId,
        String operationId,
        String key
) {

    public IdempotencyKey {
        Checks.notBlank(realm, "realm");
        Checks.notBlank(scopeKey, "scopeKey");
        Checks.notBlank(principalId, "principalId");
        Checks.notBlank(operationId, "operationId");
        Checks.notBlank(key, "key");
    }
}

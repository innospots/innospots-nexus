package com.innospots.nexus.service.runtime.idempotency;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;

/**
 * 幂等缓存项占位。二期扩展状态机。
 *
 * @param key       幂等键
 * @param fingerprint 请求指纹
 * @param createdAt 创建时间
 * @author Smars
 * @date 2026/09/15
 */
public record IdempotencyEntry(IdempotencyKey key, String fingerprint, Instant createdAt) {

    public IdempotencyEntry {
        Checks.notNull(key, "key");
        Checks.notBlank(fingerprint, "fingerprint");
        Checks.notNull(createdAt, "createdAt");
    }
}

package com.innospots.nexus.service.stream.session;

import java.time.Instant;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.security.ServiceScope;

/**
 * 流会话不可变快照。
 *
 * @param sessionId       会话标识
 * @param ownerId         创建者主体标识
 * @param scope           创建时作用域
 * @param state           当前状态
 * @param createdAt       创建时间
 * @param lastActivityAt  最近业务活动时间
 * @param bufferedItems   缓冲项数
 * @param bufferedBytes   缓冲字节
 * @author Smars
 * @date 2026/09/15
 */
public record StreamSnapshot(
        String sessionId,
        String ownerId,
        ServiceScope scope,
        StreamState state,
        Instant createdAt,
        Instant lastActivityAt,
        int bufferedItems,
        long bufferedBytes
) {

    public StreamSnapshot {
        Checks.notBlank(sessionId, "sessionId");
        Checks.notBlank(ownerId, "ownerId");
        Checks.notNull(scope, "scope");
        Checks.notNull(state, "state");
        Checks.notNull(createdAt, "createdAt");
        Checks.notNull(lastActivityAt, "lastActivityAt");
    }
}

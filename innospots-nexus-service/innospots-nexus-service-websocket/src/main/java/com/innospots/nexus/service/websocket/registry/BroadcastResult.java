package com.innospots.nexus.service.websocket.registry;

/**
 * 会话广播结果计数。
 *
 * @param attemptedCount   尝试发送的连接数
 * @param sentCount        成功发送数
 * @param rejectedCount    授权或类型拒绝数
 * @param unavailableCount 不可用连接数
 * @author Smars
 * @date 2026/09/15
 */
public record BroadcastResult(
        int attemptedCount,
        int sentCount,
        int rejectedCount,
        int unavailableCount
) {
}

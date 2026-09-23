package com.innospots.nexus.spring.service.test.fixture;

/**
 * Chat SSE {@code data} 载荷（与 stream-integration 文档示例一致）。
 *
 * @param role      角色，如 {@code assistant}
 * @param content   增量或最终文本片段
 * @param finished  是否为结束帧
 */
public record ChatStreamEvent(String role, String content, boolean finished) {
}

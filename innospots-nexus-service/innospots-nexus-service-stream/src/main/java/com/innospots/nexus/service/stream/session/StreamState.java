package com.innospots.nexus.service.stream.session;

/**
 * 流会话公共状态。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum StreamState {
    CREATED,
    OPEN,
    COMPLETED,
    FAILED,
    CANCELLED
}

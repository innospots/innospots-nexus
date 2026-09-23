package com.innospots.nexus.service.contract.channel;

/**
 * 有界通道溢出策略（HTTP 流、WebSocket 等协议模块共享）。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum OverflowPolicy {
    REJECT,
    DROP_LATEST,
    DROP_OLDEST,
    CLOSE
}

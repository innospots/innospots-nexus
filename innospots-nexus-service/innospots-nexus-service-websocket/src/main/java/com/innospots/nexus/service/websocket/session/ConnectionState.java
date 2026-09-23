package com.innospots.nexus.service.websocket.session;

/**
 * WebSocket 物理连接生命周期状态。
 *
 * @author Smars
 * @date 2026/09/15
 */
public enum ConnectionState {
    CONNECTING,
    OPEN,
    CLOSING,
    CLOSED
}

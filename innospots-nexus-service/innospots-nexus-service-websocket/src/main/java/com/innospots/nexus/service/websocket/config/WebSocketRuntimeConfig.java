package com.innospots.nexus.service.websocket.config;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.channel.OverflowPolicy;

/**
 * WebSocket 运行时配置。
 *
 * @param maxConnections              最大连接数
 * @param maxConnectionsPerUser       每主体最大连接数
 * @param maxMessageBytes             单消息字节上限
 * @param inboundBufferSize           入站队列项上限
 * @param outboundBufferSize          出站队列项上限
 * @param inboundBufferBytes          入站字节预算
 * @param outboundBufferBytes         出站字节预算
 * @param globalInboundBufferBytes    全局入站字节预算
 * @param globalOutboundBufferBytes   全局出站字节预算
 * @param overflow                    溢出策略
 * @param pingInterval                Ping 间隔
 * @param pongTimeout                 Pong 超时
 * @param idleTimeout                 业务 idle 超时
 * @param messageTimeout              单消息处理超时
 * @param inboundGovernanceEnabled    是否应用描述符上的限流/舱壁/熔断/超时；权限仍经引擎（见入站处理器）
 * @author Smars
 * @date 2026/09/15
 */
public record WebSocketRuntimeConfig(
        int maxConnections,
        int maxConnectionsPerUser,
        long maxMessageBytes,
        int inboundBufferSize,
        int outboundBufferSize,
        long inboundBufferBytes,
        long outboundBufferBytes,
        long globalInboundBufferBytes,
        long globalOutboundBufferBytes,
        OverflowPolicy overflow,
        Duration pingInterval,
        Duration pongTimeout,
        Duration idleTimeout,
        Duration messageTimeout,
        boolean inboundGovernanceEnabled
) {

    public WebSocketRuntimeConfig {
        Checks.isTrue(maxConnections > 0, "maxConnections must be positive");
        Checks.isTrue(maxConnectionsPerUser > 0, "maxConnectionsPerUser must be positive");
        Checks.isTrue(maxMessageBytes > 0, "maxMessageBytes must be positive");
        Checks.isTrue(inboundBufferSize > 0, "inboundBufferSize must be positive");
        Checks.isTrue(outboundBufferSize > 0, "outboundBufferSize must be positive");
        Checks.isTrue(inboundBufferBytes > 0, "inboundBufferBytes must be positive");
        Checks.isTrue(outboundBufferBytes > 0, "outboundBufferBytes must be positive");
        Checks.isTrue(globalInboundBufferBytes > 0, "globalInboundBufferBytes must be positive");
        Checks.isTrue(globalOutboundBufferBytes > 0, "globalOutboundBufferBytes must be positive");
        Checks.notNull(overflow, "overflow");
        Checks.notNull(pingInterval, "pingInterval");
        Checks.notNull(pongTimeout, "pongTimeout");
        Checks.notNull(idleTimeout, "idleTimeout");
        Checks.notNull(messageTimeout, "messageTimeout");
    }

    /**
     * 返回默认配置。
     *
     * @return 默认配置
     */
    public static WebSocketRuntimeConfig defaults() {
        return new WebSocketRuntimeConfig(
                10_000,
                64,
                1024L * 1024L,
                256,
                256,
                1024L * 1024L,
                1024L * 1024L,
                64L * 1024L * 1024L,
                64L * 1024L * 1024L,
                OverflowPolicy.REJECT,
                Duration.ofSeconds(30),
                Duration.ofSeconds(10),
                Duration.ofMinutes(30),
                Duration.ofSeconds(30),
                true);
    }
}

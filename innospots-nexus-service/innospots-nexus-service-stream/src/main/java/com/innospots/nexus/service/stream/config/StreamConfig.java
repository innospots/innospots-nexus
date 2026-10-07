package com.innospots.nexus.service.stream.config;

import java.time.Duration;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.contract.channel.OverflowPolicy;

/**
 * 流会话与通道配置。
 *
 * @param maxSessions           最大会话数
 * @param bufferSize            每会话队列项上限
 * @param bufferBytes           每会话字节预算
 * @param globalBufferBytes     全局字节预算
 * @param maxEventBytes         单事件字节上限
 * @param overflow              溢出策略
 * @param subscribeTimeout      未订阅超时
 * @param ttl                   会话 TTL
 * @param idleTimeout           业务 idle 超时
 * @param heartbeat             心跳间隔
 * @param terminalWriteTimeout  终态写出超时
 * @author Smars
 * @date 2026/09/15
 */
public record StreamConfig(
        int maxSessions,
        int bufferSize,
        long bufferBytes,
        long globalBufferBytes,
        long maxEventBytes,
        OverflowPolicy overflow,
        Duration subscribeTimeout,
        Duration ttl,
        Duration idleTimeout,
        Duration heartbeat,
        Duration terminalWriteTimeout
) {

    public StreamConfig {
        Checks.isTrue(maxSessions > 0, "maxSessions must be positive");
        Checks.isTrue(bufferSize > 0, "bufferSize must be positive");
        Checks.isTrue(bufferBytes > 0, "bufferBytes must be positive");
        Checks.isTrue(globalBufferBytes > 0, "globalBufferBytes must be positive");
        Checks.isTrue(maxEventBytes > 0, "maxEventBytes must be positive");
        Checks.notNull(overflow, "overflow");
        Checks.notNull(subscribeTimeout, "subscribeTimeout");
        Checks.notNull(ttl, "ttl");
        Checks.notNull(idleTimeout, "idleTimeout");
        Checks.notNull(heartbeat, "heartbeat");
        Checks.notNull(terminalWriteTimeout, "terminalWriteTimeout");
    }

    /**
     * 返回默认配置。
     *
     * @return 默认配置
     */
    public static StreamConfig defaults() {
        return new StreamConfig(
                1000,
                256,
                1024L * 1024L,
                64L * 1024L * 1024L,
                64L * 1024L,
                OverflowPolicy.REJECT,
                Duration.ofSeconds(30),
                Duration.ofMinutes(60),
                Duration.ofMinutes(10),
                Duration.ofSeconds(15),
                Duration.ofSeconds(2));
    }
}

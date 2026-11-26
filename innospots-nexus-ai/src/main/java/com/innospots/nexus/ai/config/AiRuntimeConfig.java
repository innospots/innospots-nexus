package com.innospots.nexus.ai.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

/**
 * Java 侧 AI runtime 宿主配置。
 *
 * @param dataDir               运行数据目录
 * @param configDir             Node YAML 配置目录
 * @param runtimeDir            npm 包根目录；为空时自动定位
 * @param nodeExecutable        Node 可执行文件
 * @param bindHost              仅允许回环
 * @param port                  Node 端口；0 表示随机
 * @param startupTimeout        启动超时
 * @param shutdownTimeout       关闭超时
 * @param maxRestarts           最大重启次数
 * @param startupPolicy         启动策略
 * @param healthCheckInterval   健康检查间隔
 * @param healthTimeout         单次健康检查超时
 * @param healthCacheTtl        健康结果缓存
 * @param unhealthyThreshold    连续失败阈值
 * @param restartOnUnhealthy    不健康时是否重启
 * @param configUrl             可选 ConfigSource URL
 * @param environment           白名单环境变量覆盖
 */
public record AiRuntimeConfig(
        Path dataDir,
        Path configDir,
        Path runtimeDir,
        String nodeExecutable,
        String bindHost,
        int port,
        Duration startupTimeout,
        Duration shutdownTimeout,
        int maxRestarts,
        StartupPolicy startupPolicy,
        Duration healthCheckInterval,
        Duration healthTimeout,
        Duration healthCacheTtl,
        int unhealthyThreshold,
        boolean restartOnUnhealthy,
        String configUrl,
        Map<String, String> environment) {

    /**
     * 启动策略。
     */
    public enum StartupPolicy {
        EAGER,
        LAZY
    }

    /**
     * 提供默认值并做基础校验。
     *
     * @param dataDir 数据目录
     * @return 配置
     */
    public static AiRuntimeConfig defaults(Path dataDir) {
        return new AiRuntimeConfig(
                dataDir,
                dataDir.resolve("config"),
                null,
                "node",
                "127.0.0.1",
                0,
                Duration.ofSeconds(30),
                Duration.ofSeconds(10),
                3,
                StartupPolicy.EAGER,
                Duration.ofSeconds(5),
                Duration.ofSeconds(1),
                Duration.ofSeconds(2),
                3,
                false,
                null,
                Map.of());
    }

    /**
     * 校验关键字段。
     */
    public void validate() {
        if (dataDir == null) {
            throw new IllegalArgumentException("dataDir must not be null");
        }
        if (!"127.0.0.1".equals(bindHost) && !"::1".equals(bindHost)) {
            throw new IllegalArgumentException("bindHost must be loopback");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("port out of range");
        }
        if (startupTimeout.isNegative() || startupTimeout.isZero()
                || shutdownTimeout.isNegative() || shutdownTimeout.isZero()) {
            throw new IllegalArgumentException("timeouts must be positive");
        }
    }
}

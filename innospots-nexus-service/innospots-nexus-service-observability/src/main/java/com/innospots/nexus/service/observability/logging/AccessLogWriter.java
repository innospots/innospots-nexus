package com.innospots.nexus.service.observability.logging;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.service.observability.config.ObservabilityConfig;

/**
 * 结构化访问日志写入器。
 *
 * @author Smars
 * @date 2026/09/15
 */
public final class AccessLogWriter {

    private static final Logger log = LoggerFactory.getLogger("service.access");

    private final ObservabilityConfig config;
    private final SensitiveValueMasker masker;

    /**
     * 创建写入器。
     *
     * @param config 观测配置
     * @param masker 敏感值掩码器
     */
    public AccessLogWriter(ObservabilityConfig config, SensitiveValueMasker masker) {
        this.config = Checks.notNull(config, "config");
        this.masker = Checks.notNull(masker, "masker");
    }

    /**
     * 写入终态访问日志。
     *
     * @param entry 访问日志项
     */
    public void write(AccessLogEntry entry) {
        Checks.notNull(entry, "entry");
        if (!config.accessLogEnabled()) {
            return;
        }
        log.info(
                "requestId={} method={} route={} status={} result={} durationMs={} principalId={} traceId={}",
                entry.requestId(),
                entry.method(),
                entry.routeTemplate(),
                entry.status(),
                entry.result(),
                entry.duration().toMillis(),
                masker.mask(entry.principalId()),
                masker.mask(entry.traceId()));
    }

    /**
     * 访问日志项。
     *
     * @param requestId     请求标识
     * @param method        HTTP 方法
     * @param routeTemplate 路由模板
     * @param status        HTTP 状态
     * @param result        结果分类
     * @param duration      耗时
     * @param principalId   主体标识
     * @param traceId       追踪标识
     * @param finishedAt    完成时刻
     */
    public record AccessLogEntry(
            String requestId,
            String method,
            String routeTemplate,
            int status,
            String result,
            Duration duration,
            String principalId,
            String traceId,
            Instant finishedAt
    ) {

        public AccessLogEntry {
            Checks.notBlank(requestId, "requestId");
            Checks.notBlank(method, "method");
            Checks.notBlank(routeTemplate, "routeTemplate");
            Checks.notBlank(result, "result");
            Checks.notNull(duration, "duration");
            principalId = principalId == null ? "" : principalId;
            traceId = traceId == null ? "" : traceId;
            Checks.notNull(finishedAt, "finishedAt");
        }
    }
}

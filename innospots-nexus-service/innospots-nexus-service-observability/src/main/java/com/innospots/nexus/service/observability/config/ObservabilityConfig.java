package com.innospots.nexus.service.observability.config;

import java.util.Map;
import java.util.Set;

import com.innospots.nexus.base.util.Checks;

/**
 * 观测运行时配置。
 *
 * @param accessLogEnabled         是否启用访问日志
 * @param businessMeterNames       已注册业务指标名
 * @param businessMeterTagWhitelist 业务指标标签白名单
 * @param metricLabelWhitelist     平台指标标签白名单
 * @author Smars
 * @date 2026/09/15
 */
public record ObservabilityConfig(
        boolean accessLogEnabled,
        Set<String> businessMeterNames,
        Map<String, Set<String>> businessMeterTagWhitelist,
        Set<String> metricLabelWhitelist
) {

    public ObservabilityConfig {
        businessMeterNames = businessMeterNames == null ? Set.of() : Set.copyOf(businessMeterNames);
        businessMeterTagWhitelist = businessMeterTagWhitelist == null ? Map.of() : copyTagWhitelist(businessMeterTagWhitelist);
        metricLabelWhitelist = metricLabelWhitelist == null ? Set.of() : Set.copyOf(metricLabelWhitelist);
    }

    /**
     * 返回默认配置。
     *
     * @return 默认配置
     */
    public static ObservabilityConfig defaults() {
        return new ObservabilityConfig(
                true,
                Set.of(),
                Map.of(),
                Set.of("method", "route", "status", "result", "transport", "operation", "direction", "policyKey", "reason", "state", "mode"));
    }

    private static Map<String, Set<String>> copyTagWhitelist(Map<String, Set<String>> source) {
        return source.entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> Set.copyOf(entry.getValue())));
    }
}

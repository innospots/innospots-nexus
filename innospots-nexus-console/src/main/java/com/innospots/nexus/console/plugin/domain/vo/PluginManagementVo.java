package com.innospots.nexus.console.plugin.domain.vo;

import java.time.Instant;
import java.time.LocalDateTime;

import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;

/** 插件管理页面使用的正交安装事实、运行状态和来源视图。 */
public record PluginManagementVo(
        String pluginId,
        String version,
        PluginPresence presence,
        boolean installed,
        boolean desiredEnabled,
        String runtimeState,
        String runtimePhase,
        String sourceType,
        String sourceLocation,
        String lastError,
        String definitionSnapshot,
        LocalDateTime firstDiscoveredAt,
        LocalDateTime lastDiscoveredAt,
        LocalDateTime installedAt,
        LocalDateTime enabledAt,
        LocalDateTime disabledAt,
        LocalDateTime missingAt,
        Instant runtimeDiscoveredAt,
        Instant runtimeStartedAt
) {
}

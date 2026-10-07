package com.innospots.nexus.console.plugin.domain.vo;

import java.time.Instant;
import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;

/**
 * 插件管理页面使用的正交安装事实、运行状态和来源视图。
 *
 * @author Smars
 * @date 2026/09/13
 * @param pluginId             插件标识符
 * @param version              插件版本
 * @param presence             安装存在状态
 * @param installed            是否已安装
 * @param desiredEnabled       期望启用状态
 * @param runtimeState         运行时状态
 * @param runtimePhase         运行时阶段
 * @param sourceType           来源类型
 * @param sourceLocation       来源位置
 * @param lastError            最近一次错误
 * @param definitionSnapshot   定义快照
 * @param firstDiscoveredAt    首次发现时间
 * @param lastDiscoveredAt     最近发现时间
 * @param installedAt          安装时间
 * @param enabledAt            启用时间
 * @param disabledAt           禁用时间
 * @param missingAt            缺失时间
 * @param runtimeDiscoveredAt  运行时发现时间
 * @param runtimeStartedAt     运行时启动时间
 */
@Schema(name = "PluginManagementVo", description = "插件管理视图")
public record PluginManagementVo(
        @Schema(description = "插件标识符", required = true)
        String pluginId,
        @Schema(description = "插件版本", required = true)
        String version,
        @Schema(description = "安装存在状态", required = true)
        PluginPresence presence,
        @Schema(description = "是否已安装", required = true)
        boolean installed,
        @Schema(description = "期望启用状态", required = true)
        boolean desiredEnabled,
        @Schema(description = "运行时状态")
        String runtimeState,
        @Schema(description = "运行时阶段")
        String runtimePhase,
        @Schema(description = "来源类型")
        String sourceType,
        @Schema(description = "来源位置")
        String sourceLocation,
        @Schema(description = "最近一次错误")
        String lastError,
        @Schema(description = "定义快照")
        String definitionSnapshot,
        @Schema(description = "首次发现时间")
        LocalDateTime firstDiscoveredAt,
        @Schema(description = "最近发现时间")
        LocalDateTime lastDiscoveredAt,
        @Schema(description = "安装时间")
        LocalDateTime installedAt,
        @Schema(description = "启用时间")
        LocalDateTime enabledAt,
        @Schema(description = "禁用时间")
        LocalDateTime disabledAt,
        @Schema(description = "缺失时间")
        LocalDateTime missingAt,
        @Schema(description = "运行时发现时间")
        Instant runtimeDiscoveredAt,
        @Schema(description = "运行时启动时间")
        Instant runtimeStartedAt
) {
}

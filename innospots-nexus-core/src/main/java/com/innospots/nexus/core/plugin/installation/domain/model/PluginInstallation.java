package com.innospots.nexus.core.plugin.installation.domain.model;

import java.time.LocalDateTime;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginPresence;
import com.innospots.nexus.core.plugin.installation.domain.enums.PluginSourceType;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 插件安装事实与管理员意图的不可变领域模型。
 *
 * @param installationId 安装记录主键
 * @param pluginId 稳定的插件标识
 * @param pluginVersion 已安装或最近发现的插件版本
 * @param sourceType 定义来源类型
 * @param sourceLocation 定义来源位置
 * @param presence 当前有效目录中的存在性
 * @param installed 是否已执行安装动作
 * @param desiredEnabled 管理员是否期望启用
 * @param definitionSnapshot 脱敏后的定义 JSON 快照
 * @param lastRuntimeState 最近持久化的运行状态
 * @param lastError 最近持久化的脱敏错误
 * @param firstDiscoveredAt 首次发现时间
 * @param lastDiscoveredAt 最近一次发现时间
 * @param installedAt 安装时间；未安装时为 null
 * @param enabledAt 最近一次启用时间
 * @param disabledAt 最近一次禁用时间
 * @param missingAt 标记为缺失的时间
 */
public record PluginInstallation(
        String installationId,
        String pluginId,
        String pluginVersion,
        PluginSourceType sourceType,
        String sourceLocation,
        PluginPresence presence,
        boolean installed,
        boolean desiredEnabled,
        String definitionSnapshot,
        String lastRuntimeState,
        String lastError,
        LocalDateTime firstDiscoveredAt,
        LocalDateTime lastDiscoveredAt,
        LocalDateTime installedAt,
        LocalDateTime enabledAt,
        LocalDateTime disabledAt,
        LocalDateTime missingAt
) {

    /**
     * @throws NexusException 未安装插件不能标记为期望启用时抛出
     */
    public PluginInstallation {
        if (!installed && desiredEnabled) {
            throw NexusException.build(
                    PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "an uninstalled plugin cannot be desired enabled");
        }
    }
}

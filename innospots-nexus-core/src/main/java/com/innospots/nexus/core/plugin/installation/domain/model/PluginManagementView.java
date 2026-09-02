package com.innospots.nexus.core.plugin.installation.domain.model;

import java.time.Instant;
import java.util.Optional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.core.plugin.lifecycle.PluginRuntimeInfo;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 安装事实、管理员意图与当前 JVM 运行事实的聚合只读视图。
 *
 * @param installation 持久化的安装事实
 * @param runtime 当前 JVM 中的运行快照；未加载运行时时为空
 */
public record PluginManagementView(
        PluginInstallation installation,
        Optional<PluginRuntimeInfo> runtime
) {

    /**
     * @throws NexusException 安装事实或运行时容器为空时抛出
     */
    public PluginManagementView {
        if (installation == null || runtime == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_PERSISTENCE_FAILED,
                    "plugin management view requires installation and runtime values");
        }
        runtime = runtime.isEmpty() ? Optional.empty() : Optional.of(runtime.orElseThrow());
    }

    /**
     * 返回稳定插件身份。
     *
     * @return 插件标识
     */
    public String pluginId() {
        return installation.pluginId();
    }

    /**
     * 返回持久化的最近运行状态；运行时尚未创建时为空。
     *
     * @return 当前或最近持久化的运行状态名称
     */
    public String runtimeState() {
        return runtime.map(PluginRuntimeInfo::state)
                .map(Enum::name)
                .orElse(installation.lastRuntimeState());
    }

    /**
     * 返回持久化或运行时诊断中的最近错误。
     *
     * @return 脱敏后的最近错误；无错误时可能为 null
     */
    public String lastError() {
        return runtime.map(PluginRuntimeInfo::lastError).orElse(installation.lastError());
    }

    /**
     * 返回当前运行阶段。
     *
     * @return 运行时阶段；未加载运行时时为 null
     */
    public String runtimePhase() {
        return runtime.map(PluginRuntimeInfo::phase).orElse(null);
    }

    /**
     * 返回当前运行时记录的发现时间。
     *
     * @return 运行时发现时间；未加载运行时时为 null
     */
    public Instant runtimeDiscoveredAt() {
        return runtime.map(PluginRuntimeInfo::discoveredAt).orElse(null);
    }

    /**
     * 返回当前运行时记录的启动时间。
     *
     * @return 运行时启动时间；未启动或未加载运行时时为 null
     */
    public Instant runtimeStartedAt() {
        return runtime.map(PluginRuntimeInfo::startedAt).orElse(null);
    }
}

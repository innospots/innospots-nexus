package com.innospots.nexus.core.plugin.discovery;

import java.util.List;

import com.innospots.nexus.core.plugin.declaration.PluginSource;

/**
 * 单个插件定义被拒绝时保留的来源和安全诊断。
 *
 * @param source 被拒绝定义的来源元数据
 * @param claimedPluginId 声明的插件标识；未知时可为空
 * @param diagnostics 脱敏后的拒绝原因列表；{@code null} 视为空列表
 */
public record RejectedPluginDefinition(
        PluginSource source,
        String claimedPluginId,
        List<String> diagnostics
) {

    public RejectedPluginDefinition {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }
}

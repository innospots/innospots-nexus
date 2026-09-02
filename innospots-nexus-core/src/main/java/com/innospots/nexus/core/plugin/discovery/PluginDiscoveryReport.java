package com.innospots.nexus.core.plugin.discovery;

import java.util.List;

/**
 * 表示发现阶段成功目录与被拒绝定义的分离结果。
 *
 * @param validCatalog 通过全局校验的插件目录
 * @param rejectedDefinitions 被拒绝定义的不可变诊断列表；{@code null} 视为空列表
 */
public record PluginDiscoveryReport(
        PluginCatalog validCatalog,
        List<RejectedPluginDefinition> rejectedDefinitions
) {

    public PluginDiscoveryReport {
        rejectedDefinitions = rejectedDefinitions == null ? List.of() : List.copyOf(rejectedDefinitions);
    }
}

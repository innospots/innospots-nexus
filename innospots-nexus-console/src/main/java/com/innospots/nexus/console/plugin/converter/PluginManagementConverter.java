package com.innospots.nexus.console.plugin.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.innospots.nexus.base.mapstruct.BaseMapperConfig;
import com.innospots.nexus.console.plugin.domain.vo.PluginManagementVo;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;

/** 使用 MapStruct 将 Core 管理聚合视图转换为 Console VO。 */
@Mapper(config = BaseMapperConfig.class)
public interface PluginManagementConverter {

    /** 默认的无状态转换器实例。 */
    PluginManagementConverter INSTANCE = Mappers.getMapper(PluginManagementConverter.class);

    /** 转换安装事实、运行快照和诊断字段。 */
    @Mapping(source = "installation.pluginId", target = "pluginId")
    @Mapping(source = "installation.pluginVersion", target = "version")
    @Mapping(source = "installation.presence", target = "presence")
    @Mapping(source = "installation.installed", target = "installed")
    @Mapping(source = "installation.desiredEnabled", target = "desiredEnabled")
    @Mapping(source = "installation.sourceType", target = "sourceType")
    @Mapping(source = "installation.sourceLocation", target = "sourceLocation")
    @Mapping(source = "installation.definitionSnapshot", target = "definitionSnapshot")
    @Mapping(source = "installation.firstDiscoveredAt", target = "firstDiscoveredAt")
    @Mapping(source = "installation.lastDiscoveredAt", target = "lastDiscoveredAt")
    @Mapping(source = "installation.installedAt", target = "installedAt")
    @Mapping(source = "installation.enabledAt", target = "enabledAt")
    @Mapping(source = "installation.disabledAt", target = "disabledAt")
    @Mapping(source = "installation.missingAt", target = "missingAt")
    @Mapping(target = "runtimeState", expression = "java(view.runtimeState())")
    @Mapping(target = "lastError", expression = "java(view.lastError())")
    @Mapping(target = "runtimePhase", expression = "java(view.runtime().map(runtime -> runtime.phase()).orElse(null))")
    @Mapping(target = "runtimeDiscoveredAt", expression = "java(view.runtime().map(runtime -> runtime.discoveredAt()).orElse(null))")
    @Mapping(target = "runtimeStartedAt", expression = "java(view.runtime().map(runtime -> runtime.startedAt()).orElse(null))")
    PluginManagementVo toVo(PluginManagementView view);
}

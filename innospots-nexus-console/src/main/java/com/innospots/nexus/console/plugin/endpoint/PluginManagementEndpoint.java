package com.innospots.nexus.console.plugin.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.domain.vo.PluginManagementVo;
import com.innospots.nexus.core.plugin.installation.domain.model.PluginManagementView;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/** 管理端插件查询、安装、启停和失败重试接口；不提供 JAR 删除或卸载操作。 */
@Path("/console/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class PluginManagementEndpoint {

    private final PluginInstallationManager manager;
    private final PluginManagementConverter converter;

    /** 创建只依赖 Core 安装管理器的插件管理接口。 */
    public PluginManagementEndpoint(PluginInstallationManager manager) {
        this(manager, PluginManagementConverter.INSTANCE);
    }

    /** 创建可注入转换器的插件管理接口，便于无数据库测试。 */
    public PluginManagementEndpoint(
            PluginInstallationManager manager,
            PluginManagementConverter converter
    ) {
        if (manager == null || converter == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin manager and converter are required");
        }
        this.manager = manager;
        this.converter = converter;
    }

    /** 查询全部插件聚合视图。 */
    @GET
    public R<List<PluginManagementVo>> list() {
        return R.ok(manager.plugins().stream().map(converter::toVo).toList());
    }

    /** 查询单个插件聚合视图。 */
    @GET
    @Path("/{pluginId}")
    public R<PluginManagementVo> get(@PathParam("pluginId") String pluginId) {
        return R.ok(manager.plugin(pluginId).map(converter::toVo).orElseThrow(
                () -> NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin was not found: " + pluginId)));
    }

    /** 安装并启动插件。 */
    @POST
    @Path("/{pluginId}/install")
    public R<PluginManagementVo> install(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager.installAndStart(pluginId)));
    }

    /** 启用已安装插件。 */
    @POST
    @Path("/{pluginId}/enable")
    public R<PluginManagementVo> enable(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager.enable(pluginId)));
    }

    /** 停用插件但保留安装事实。 */
    @POST
    @Path("/{pluginId}/disable")
    public R<PluginManagementVo> disable(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager.disable(pluginId)));
    }

    /** 重试处于 FAILED 的插件。 */
    @POST
    @Path("/{pluginId}/retry")
    public R<PluginManagementVo> retry(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager.retryStart(pluginId)));
    }
}

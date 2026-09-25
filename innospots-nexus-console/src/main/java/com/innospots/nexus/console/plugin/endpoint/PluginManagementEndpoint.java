package com.innospots.nexus.console.plugin.endpoint;

import java.util.List;
import java.util.function.Supplier;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.domain.vo.PluginManagementVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 管理端插件查询、安装、启停和失败重试接口；不提供 JAR 删除或卸载操作。
 */
@Path("/console/plugins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Plugin", description = "插件生命周期")
@NexusAuthenticatedApi
public final class PluginManagementEndpoint {

    private final Supplier<PluginInstallationManager> managerSupplier;
    private final PluginManagementConverter converter;
    private final ConsoleCatalogSyncService syncService;

    public PluginManagementEndpoint(PluginInstallationManager manager) {
        this(() -> manager, PluginManagementConverter.INSTANCE, null);
    }

    public PluginManagementEndpoint(
            PluginInstallationManager manager,
            PluginManagementConverter converter
    ) {
        this(() -> manager, converter, null);
    }

    public PluginManagementEndpoint(
            PluginInstallationManager manager,
            PluginManagementConverter converter,
            ConsoleCatalogSyncService syncService
    ) {
        this(() -> manager, converter, syncService);
    }

    /**
     * 通过 {@link Supplier} 延迟解析安装管理器，便于 Spring 在插件子系统启动完成前注册 JAX-RS 资源。
     */
    public PluginManagementEndpoint(
            Supplier<PluginInstallationManager> managerSupplier,
            PluginManagementConverter converter,
            ConsoleCatalogSyncService syncService) {
        if (managerSupplier == null || converter == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin manager supplier and converter are required");
        }
        this.managerSupplier = managerSupplier;
        this.converter = converter;
        this.syncService = syncService;
    }

    private PluginInstallationManager manager() {
        PluginInstallationManager current = managerSupplier.get();
        if (current == null) {
            throw NexusException.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin subsystem has not started yet");
        }
        return current;
    }

    @GET
    @Operation(operationId = "pluginList", summary = "查询全部插件")
    public R<List<PluginManagementVo>> list() {
        return R.ok(manager().plugins().stream().map(converter::toVo).toList());
    }

    @GET
    @Path("/{pluginId}")
    @Operation(operationId = "pluginGet", summary = "查询单个插件")
    public R<PluginManagementVo> get(@PathParam("pluginId") String pluginId) {
        return R.ok(manager().plugin(pluginId).map(converter::toVo).orElseThrow(
                () -> NexusException.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin was not found: " + pluginId)));
    }

    @POST
    @Path("/{pluginId}/install")
    @Operation(operationId = "pluginInstall", summary = "安装并启动插件")
    public R<PluginManagementVo> install(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager().installAndStart(pluginId)));
    }

    @POST
    @Path("/{pluginId}/enable")
    @Operation(operationId = "pluginEnable", summary = "启用插件")
    public R<PluginManagementVo> enable(@PathParam("pluginId") String pluginId) {
        PluginManagementVo result = converter.toVo(manager().enable(pluginId));
        syncCatalog();
        return R.ok(result);
    }

    @POST
    @Path("/{pluginId}/disable")
    @Operation(operationId = "pluginDisable", summary = "停用插件")
    public R<PluginManagementVo> disable(@PathParam("pluginId") String pluginId) {
        PluginManagementVo result = converter.toVo(manager().disable(pluginId));
        syncCatalog();
        return R.ok(result);
    }

    @POST
    @Path("/{pluginId}/retry")
    @Operation(operationId = "pluginRetry", summary = "重试失败插件")
    public R<PluginManagementVo> retry(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager().retryStart(pluginId)));
    }

    private void syncCatalog() {
        if (syncService != null) {
            syncService.sync();
        }
    }
}

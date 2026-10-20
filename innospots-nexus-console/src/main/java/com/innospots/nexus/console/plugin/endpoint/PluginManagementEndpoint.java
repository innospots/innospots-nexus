package com.innospots.nexus.console.plugin.endpoint;

import java.util.List;
import java.util.function.Supplier;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogSyncService;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;
import com.innospots.nexus.console.plugin.converter.PluginManagementConverter;
import com.innospots.nexus.console.plugin.domain.vo.PluginManagementVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.core.plugin.installation.service.PluginInstallationManager;
import com.innospots.nexus.core.plugin.status.PluginStatusCode;

/**
 * 管理端插件查询、安装、启停和失败重试接口；不提供 JAR 删除或卸载操作。
 */
@EndpointModule("plugin")
@Path(ConsoleConstant.API_PREFIX + "/plugins")
@Produces(MediaType.APPLICATION_JSON)
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
            throw ConsoleExceptions.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin manager supplier and converter are required");
        }
        this.managerSupplier = managerSupplier;
        this.converter = converter;
        this.syncService = syncService;
    }

    private PluginInstallationManager manager() {
        PluginInstallationManager current = managerSupplier.get();
        if (current == null) {
            throw ConsoleExceptions.build(PluginStatusCode.PLUGIN_CONFIG_INVALID,
                    "plugin subsystem has not started yet");
        }
        return current;
    }

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "plugin.list",
            label = @I18nLiteral(pairs = {"zh-CN", "查询全部插件", "en", "List plugins"}))
    @Operation(operationId = "pluginList", summary = "查询全部插件")
    public R<List<PluginManagementVo>> list(
            @QueryParam("keyword") String keyword,
            @QueryParam("installed") String installed
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();
        String normalizedInstalled = installed == null ? "" : installed.trim();
        return R.ok(manager().plugins().stream()
                .map(converter::toVo)
                .filter(vo -> matchesInstalledFilter(vo, normalizedInstalled))
                .filter(vo -> matchesKeyword(vo, normalizedKeyword))
                .toList());
    }

    private static boolean matchesInstalledFilter(PluginManagementVo vo, String installed) {
        if (installed.isEmpty() || "all".equalsIgnoreCase(installed)) {
            return true;
        }
        if ("installed".equalsIgnoreCase(installed)) {
            return vo.installed();
        }
        if ("notInstalled".equalsIgnoreCase(installed)) {
            return !vo.installed();
        }
        return true;
    }

    private static boolean matchesKeyword(PluginManagementVo vo, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return containsIgnoreCase(vo.pluginName(), keyword)
                || containsIgnoreCase(vo.pluginId(), keyword);
    }

    private static boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    @GET
    @Path("/{pluginId}")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "plugin.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询单个插件", "en", "Get plugin"}))
    @Operation(operationId = "pluginGet", summary = "查询单个插件")
    public R<PluginManagementVo> get(@PathParam("pluginId") String pluginId) {
        return R.ok(manager().plugin(pluginId).map(converter::toVo).orElseThrow(
                () -> ConsoleExceptions.build(PluginStatusCode.PLUGIN_NOT_INSTALLED,
                        "plugin was not found: " + pluginId)));
    }

    @POST
    @Path("/{pluginId}/install")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "plugin.install",
            label = @I18nLiteral(pairs = {"zh-CN", "安装并启动插件", "en", "Install and start plugin"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "pluginInstall", summary = "安装并启动插件")
    public R<PluginManagementVo> install(@PathParam("pluginId") String pluginId) {
        return R.ok(converter.toVo(manager().installAndStart(pluginId)));
    }

    @POST
    @Path("/{pluginId}/enable")
    @EndpointGrant(
            action = EndpointGrantAction.STATUS,
            name = "plugin.enable",
            label = @I18nLiteral(pairs = {"zh-CN", "启用插件", "en", "Enable plugin"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "pluginEnable", summary = "启用插件")
    public R<PluginManagementVo> enable(@PathParam("pluginId") String pluginId) {
        PluginManagementVo result = converter.toVo(manager().enable(pluginId));
        syncCatalog();
        return R.ok(result);
    }

    @POST
    @Path("/{pluginId}/disable")
    @EndpointGrant(
            action = EndpointGrantAction.STATUS,
            name = "plugin.disable",
            label = @I18nLiteral(pairs = {"zh-CN", "停用插件", "en", "Disable plugin"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "pluginDisable", summary = "停用插件")
    public R<PluginManagementVo> disable(@PathParam("pluginId") String pluginId) {
        PluginManagementVo result = converter.toVo(manager().disable(pluginId));
        syncCatalog();
        return R.ok(result);
    }

    @POST
    @Path("/{pluginId}/retry")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "plugin.retry",
            label = @I18nLiteral(pairs = {"zh-CN", "重试失败插件", "en", "Retry failed plugin"}),
            recordArgs = false,
            auditLog = true)
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

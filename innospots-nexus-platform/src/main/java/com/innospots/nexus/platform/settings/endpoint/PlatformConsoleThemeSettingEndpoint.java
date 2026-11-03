package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleThemeUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleThemeSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleThemeSettingService;

/**
 * 运营平台设置 — 控制台配色主题。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_THEME_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleTheme", description = "平台设置：控制台配色主题")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleThemeSettingEndpoint {

    private final PlatformConsoleThemeSettingService consoleThemeSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-theme.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询控制台配色主题", "en", "Get console theme setting"}))
    @Operation(operationId = "platformConsoleThemeSettingsGet", summary = "查询控制台配色主题")
    public R<PlatformConsoleThemeSettingVo> getConsoleThemeSetting() {
        return R.ok(consoleThemeSettingService.getConsoleThemeSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.settings.console-theme.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新控制台配色主题", "en", "Update console theme setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformConsoleThemeSettingsUpdate", summary = "更新控制台配色主题")
    public R<PlatformConsoleThemeSettingVo> updateConsoleThemeSetting(
            PlatformConsoleThemeUpdateRequest request) {
        return R.ok(consoleThemeSettingService.updateConsoleThemeSetting(request));
    }
}

package com.innospots.nexus.platform.settings.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
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
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleGeneralSettingsVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;

/**
 * 运营平台设置 — 控制台基础系统设置（聚合读，供系统设置页加载）。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_GENERAL_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleGeneral", description = "平台设置：控制台基础系统设置")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleGeneralSettingsEndpoint {

    private final PlatformConsoleGeneralSettingService consoleGeneralSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-general.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询控制台基础系统设置", "en", "Get console general settings"}))
    @Operation(operationId = "platformConsoleGeneralSettingsGet", summary = "查询控制台基础系统设置")
    public R<PlatformConsoleGeneralSettingsVo> getConsoleGeneralSettings() {
        return R.ok(consoleGeneralSettingService.getConsoleGeneralSettings());
    }
}

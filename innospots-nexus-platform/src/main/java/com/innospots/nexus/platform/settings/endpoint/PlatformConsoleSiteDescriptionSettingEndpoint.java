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
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteDescriptionUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleSiteDescriptionSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;

/**
 * 运营平台设置 — 系统简介。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_SITE_DESCRIPTION_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleSiteDescription", description = "平台设置：系统简介")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleSiteDescriptionSettingEndpoint {

    private final PlatformConsoleGeneralSettingService consoleGeneralSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-site-description.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询系统简介", "en", "Get console site description setting"}))
    @Operation(operationId = "platformConsoleSiteDescriptionSettingsGet", summary = "查询系统简介")
    public R<PlatformConsoleSiteDescriptionSettingVo> getSiteDescriptionSetting() {
        return R.ok(consoleGeneralSettingService.getSiteDescriptionSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.settings.console-site-description.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新系统简介", "en", "Update console site description setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformConsoleSiteDescriptionSettingsUpdate", summary = "更新系统简介")
    public R<PlatformConsoleSiteDescriptionSettingVo> updateSiteDescriptionSetting(
            PlatformConsoleSiteDescriptionUpdateRequest request) {
        return R.ok(consoleGeneralSettingService.updateSiteDescriptionSetting(request));
    }
}

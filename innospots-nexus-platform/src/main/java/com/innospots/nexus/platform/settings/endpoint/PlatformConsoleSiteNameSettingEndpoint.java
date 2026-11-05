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
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleSiteNameUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleSiteNameSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;

/**
 * 运营平台设置 — 系统名称。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_SITE_NAME_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleSiteName", description = "平台设置：系统名称")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleSiteNameSettingEndpoint {

    private final PlatformConsoleGeneralSettingService consoleGeneralSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-site-name.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询系统名称", "en", "Get console site name setting"}))
    @Operation(operationId = "platformConsoleSiteNameSettingsGet", summary = "查询系统名称")
    public R<PlatformConsoleSiteNameSettingVo> getSiteNameSetting() {
        return R.ok(consoleGeneralSettingService.getSiteNameSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            group = "update",
            name = "platform.settings.console-site-name.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新系统名称", "en", "Update console site name setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformConsoleSiteNameSettingsUpdate", summary = "更新系统名称")
    public R<PlatformConsoleSiteNameSettingVo> updateSiteNameSetting(PlatformConsoleSiteNameUpdateRequest request) {
        return R.ok(consoleGeneralSettingService.updateSiteNameSetting(request));
    }
}

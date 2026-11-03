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
import com.innospots.nexus.platform.settings.domain.request.PlatformNavigationLayoutUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformNavigationLayoutSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformNavigationLayoutSettingService;

/**
 * 运营平台设置 — 控制台导航布局（左轨双栏 / 单栏分组）。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_NAVIGATION_LAYOUT_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsNavigationLayout", description = "平台设置：控制台导航布局")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformNavigationLayoutSettingEndpoint {

    private final PlatformNavigationLayoutSettingService navigationLayoutSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.navigation-layout.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询控制台导航布局", "en", "Get navigation layout setting"}))
    @Operation(operationId = "platformNavigationLayoutSettingsGet", summary = "查询控制台导航布局")
    public R<PlatformNavigationLayoutSettingVo> getNavigationLayoutSetting() {
        return R.ok(navigationLayoutSettingService.getNavigationLayoutSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.settings.navigation-layout.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新控制台导航布局", "en", "Update navigation layout setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformNavigationLayoutSettingsUpdate", summary = "更新控制台导航布局")
    public R<PlatformNavigationLayoutSettingVo> updateNavigationLayoutSetting(
            PlatformNavigationLayoutUpdateRequest request) {
        return R.ok(navigationLayoutSettingService.updateNavigationLayoutSetting(request));
    }
}

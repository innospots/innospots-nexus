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
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleTimezoneUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleTimezoneSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;

/**
 * 运营平台设置 — 控制台默认时区。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_TIMEZONE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleTimezone", description = "平台设置：默认时区")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleTimezoneSettingEndpoint {

    private final PlatformConsoleGeneralSettingService consoleGeneralSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-timezone.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询默认时区", "en", "Get console timezone setting"}))
    @Operation(operationId = "platformConsoleTimezoneSettingsGet", summary = "查询默认时区")
    public R<PlatformConsoleTimezoneSettingVo> getTimezoneSetting() {
        return R.ok(consoleGeneralSettingService.getTimezoneSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            group = "update",
            name = "platform.settings.console-timezone.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新默认时区", "en", "Update console timezone setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformConsoleTimezoneSettingsUpdate", summary = "更新默认时区")
    public R<PlatformConsoleTimezoneSettingVo> updateTimezoneSetting(PlatformConsoleTimezoneUpdateRequest request) {
        return R.ok(consoleGeneralSettingService.updateTimezoneSetting(request));
    }
}

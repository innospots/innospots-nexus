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
import com.innospots.nexus.platform.settings.domain.request.PlatformConsoleDisplayLanguageUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformConsoleDisplayLanguageSettingVo;
import com.innospots.nexus.platform.settings.service.PlatformConsoleGeneralSettingService;

/**
 * 运营平台设置 — 控制台界面语言。
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.SYSTEM_DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_CONSOLE_DISPLAY_LANGUAGE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsConsoleDisplayLanguage", description = "平台设置：界面语言")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformConsoleDisplayLanguageSettingEndpoint {

    private final PlatformConsoleGeneralSettingService consoleGeneralSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.console-display-language.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询界面语言", "en", "Get console display language setting"}))
    @Operation(operationId = "platformConsoleDisplayLanguageSettingsGet", summary = "查询界面语言")
    public R<PlatformConsoleDisplayLanguageSettingVo> getDisplayLanguageSetting() {
        return R.ok(consoleGeneralSettingService.getDisplayLanguageSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.settings.console-display-language.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新界面语言", "en", "Update console display language setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformConsoleDisplayLanguageSettingsUpdate", summary = "更新界面语言")
    public R<PlatformConsoleDisplayLanguageSettingVo> updateDisplayLanguageSetting(
            PlatformConsoleDisplayLanguageUpdateRequest request) {
        return R.ok(consoleGeneralSettingService.updateDisplayLanguageSetting(request));
    }
}

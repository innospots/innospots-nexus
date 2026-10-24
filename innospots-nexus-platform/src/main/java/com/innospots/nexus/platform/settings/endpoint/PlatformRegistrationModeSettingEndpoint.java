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
import com.innospots.nexus.platform.settings.domain.request.PlatformRegistrationPolicyUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformRegistrationPolicyVo;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

/**
 * 运营平台设置 — 自助注册模式（管理端）。
 *
 * @author Smars
 * @date 2026/10/06
 */
@EndpointModule(value = "settings", domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY)
@Path(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsRegistrationMode", description = "平台设置：自助注册模式")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRegistrationModeSettingEndpoint {

    private final PlatformRegistrationModeSettingService registrationModeSettingService;

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.settings.registration-mode.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询自助注册模式设置", "en", "Get registration mode setting"}))
    @Operation(operationId = "platformRegistrationSettingsGet", summary = "查询自助注册模式设置")
    public R<PlatformRegistrationPolicyVo> getRegistrationModeSetting() {
        return R.ok(registrationModeSettingService.getRegistrationModeSetting());
    }

    @PUT
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.settings.registration-mode.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新自助注册模式设置", "en", "Update registration mode setting"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "platformRegistrationSettingsUpdate", summary = "更新自助注册模式设置")
    public R<PlatformRegistrationPolicyVo> updateRegistrationModeSetting(
            PlatformRegistrationPolicyUpdateRequest request) {
        return R.ok(registrationModeSettingService.updateRegistrationModeSetting(request));
    }
}

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
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.settings.domain.request.PlatformRegistrationPolicyUpdateRequest;
import com.innospots.nexus.platform.settings.domain.vo.PlatformRegistrationPolicyVo;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

/**
 * 运营平台设置 — 自助注册模式（管理端）。
 *
 * @author Smars
 * @date 2026/10/06
 */
@Path(PlatformConstant.SETTINGS_REGISTRATION_MODE_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformSettingsRegistrationMode", description = "平台设置：自助注册模式")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformRegistrationModeSettingEndpoint {

    private final PlatformRegistrationModeSettingService registrationModeSettingService;

    @GET
    @Operation(operationId = "platformRegistrationSettingsGet", summary = "查询自助注册模式设置")
    public R<PlatformRegistrationPolicyVo> getRegistrationModeSetting() {
        return R.ok(registrationModeSettingService.getRegistrationModeSetting());
    }

    @PUT
    @Operation(operationId = "platformRegistrationSettingsUpdate", summary = "更新自助注册模式设置")
    public R<PlatformRegistrationPolicyVo> updateRegistrationModeSetting(
            PlatformRegistrationPolicyUpdateRequest request) {
        return R.ok(registrationModeSettingService.updateRegistrationModeSetting(request));
    }
}

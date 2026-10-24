package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.endpoint.PublicPasswordResetEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;

/**
 * 运营管理平台匿名密码重置 REST。
 */
@EndpointModule(
        value = "auth",
        domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY,
        ignoreAuth = true)
@Path(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicPasswordReset", description = "运营管理平台密码重置")
@RequiredArgsConstructor
public class PlatformPublicPasswordResetEndpoint implements PublicPasswordResetEndpoint {

    private final PlatformPasswordService platformPasswordService;

    @Override
    public R<Void> resetPassword(PasswordResetRequest request) {
        platformPasswordService.resetPassword(request);
        return R.ok();
    }
}

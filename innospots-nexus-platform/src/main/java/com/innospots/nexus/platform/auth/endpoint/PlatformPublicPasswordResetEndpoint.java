package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.endpoint.PublicPasswordResetEndpoint;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 运营管理平台匿名密码重置 REST。
 */
@Path(PlatformConstant.PUBLIC_AUTH_PASSWORD_PATH)
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

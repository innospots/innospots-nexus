package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.endpoint.AuthSessionEndpoint;
import com.innospots.nexus.platform.auth.service.PlatformAuthService;
import com.innospots.nexus.platform.auth.service.PlatformPasswordService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 运营管理平台已登录会话 REST（刷新、登出、改密）。
 */
@Path(PlatformConstant.AUTH_PATH)
@Tag(name = "PlatformAuthSession", description = "运营管理平台会话")
@RequiredArgsConstructor
public class PlatformAuthSessionEndpoint implements AuthSessionEndpoint {

    private final PlatformAuthService platformAuthService;
    private final PlatformPasswordService platformPasswordService;

    @Override
    public R<AuthTokenVo> refresh(TokenRefreshRequest request) {
        return R.ok(platformAuthService.refresh(request));
    }

    @Override
    public R<Void> logout() {
        platformAuthService.logout();
        return R.ok();
    }

    @Override
    public R<Void> changePassword(PasswordChangeRequest request) {
        platformPasswordService.changePassword(request);
        return R.ok();
    }
}

package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.AuthCaptchaIssueRequest;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.vo.AuthRsaPublicKeyVo;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.auth.endpoint.PublicAuthEndpoint;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.platform.auth.service.PlatformAuthService;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;

/**
 * 运营管理平台匿名认证 REST（登录、验证码）。
 */
@EndpointModule(
        value = "auth",
        domain = BuiltinPlatformEntryPlugins.DOMAIN_KEY,
        ignoreAuth = true)
@Path(PlatformConstant.PUBLIC_AUTH_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicAuth", description = "运营管理平台匿名认证")
@RequiredArgsConstructor
public class PlatformPublicAuthEndpoint implements PublicAuthEndpoint {

    private final PlatformAuthService platformAuthService;

    @Override
    public R<AuthRsaPublicKeyVo> getPasswordEncryptionPublicKey() {
        return R.ok(platformAuthService.getPasswordEncryptionPublicKey());
    }

    @Override
    public R<AuthCaptchaVo> issueLoginCaptcha(AuthCaptchaIssueRequest request) {
        return R.ok(platformAuthService.issueLoginCaptcha(request));
    }

    @Override
    public R<AuthTokenVo> login(AuthLoginRequest request) {
        return R.ok(platformAuthService.login(request));
    }
}

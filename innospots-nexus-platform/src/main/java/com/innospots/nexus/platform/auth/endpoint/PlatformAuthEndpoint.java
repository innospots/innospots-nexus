package com.innospots.nexus.platform.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.request.AuthCaptchaIssueRequest;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.request.PasswordChangeRequest;
import com.innospots.nexus.console.auth.domain.request.PasswordResetRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.core.openapi.NexusOpenApiSecurityNames;
import com.innospots.nexus.platform.auth.operator.PlatformPasswordOperator;

/**
 * 运营管理平台认证 REST 资源。
 *
 * <p>不提供公开自助注册；登录、刷新与验证码为匿名接口，登出与改密需 PLATFORM Bearer 令牌。</p>
 */
@Path(PlatformConstant.AUTH_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformAuth", description = "运营管理平台认证")
@RequiredArgsConstructor
public class PlatformAuthEndpoint {

    private final AuthFacade authFacade;
    private final PlatformPasswordOperator passwordOperator;
    private final PasswordDecryptor passwordDecryptor;

    @POST
    @Path("/captcha/issue")
    @Operation(operationId = "platformAuthCaptchaIssue", summary = "签发登录验证码")
    public R<AuthCaptchaVo> issueLoginCaptcha(AuthCaptchaIssueRequest request) {
        String clientKey = request == null ? null : request.clientKey();
        return R.ok(authFacade.issueLoginCaptcha(clientKey));
    }

    @POST
    @Path("/login")
    @Operation(operationId = "platformAuthLogin", summary = "运营管理平台登录")
    public R<AuthTokenVo> login(AuthLoginRequest request) {
        return R.ok(authFacade.login(request));
    }

    @POST
    @Path("/refresh")
    @Operation(operationId = "platformAuthRefresh", summary = "刷新访问令牌")
    public R<AuthTokenVo> refresh(TokenRefreshRequest request) {
        return R.ok(authFacade.refresh(request));
    }

    @POST
    @Path("/logout")
    @Operation(operationId = "platformAuthLogout", summary = "登出当前会话")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    public R<Void> logout() {
        authFacade.logout();
        return R.ok();
    }

    @POST
    @Path("/password/change")
    @Operation(operationId = "platformAuthPasswordChange", summary = "修改当前用户密码")
    @SecurityRequirement(name = NexusOpenApiSecurityNames.BEARER_AUTH)
    public R<Void> changePassword(PasswordChangeRequest request) {
        Checks.notNull(request, "request");
        String platformUserId = requirePlatformUserId();
        String oldPassword = passwordDecryptor.decrypt(request.oldEncryptedPassword());
        String newPassword = passwordDecryptor.decrypt(request.newEncryptedPassword());
        passwordOperator.changePassword(platformUserId, oldPassword, newPassword);
        return R.ok();
    }

    @POST
    @Path("/password/reset")
    @Operation(operationId = "platformAuthPasswordReset", summary = "凭验证码重置密码")
    public R<Void> resetPassword(PasswordResetRequest request) {
        Checks.notNull(request, "request");
        String newPassword = passwordDecryptor.decrypt(request.newEncryptedPassword());
        passwordOperator.resetPassword(
                request.identity(),
                request.verificationCode(),
                request.type(),
                newPassword);
        return R.ok();
    }

    private static String requirePlatformUserId() {
        String platformUserId = TLC.platformUserId();
        if (platformUserId == null || platformUserId.isBlank()) {
            platformUserId = TLC.getString(TLC.USER_ID);
        }
        if (platformUserId == null || platformUserId.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        return platformUserId;
    }
}

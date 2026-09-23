package com.innospots.nexus.kernel.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
import com.innospots.nexus.kernel.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.kernel.auth.domain.request.TenantRegisterRequest;
import com.innospots.nexus.kernel.auth.service.TenantAuthFacade;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.kernel.user.domain.request.UserPasswordRegisterRequest;
import com.innospots.nexus.kernel.user.operator.PasswordOperator;
import com.innospots.nexus.kernel.user.operator.UserOperator;

/**
 * 租户域认证与身份注册 REST 资源。
 */
@Path("/tenant/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "TenantAuth", description = "租户域认证与注册")
@RequiredArgsConstructor
public class TenantAuthEndpoint {

    private final TenantAuthFacade tenantAuthFacade;
    private final UserOperator userOperator;
    private final PasswordOperator passwordOperator;
    private final PasswordDecryptor passwordDecryptor;

    @POST
    @Path("/register")
    public R<AuthTokenVo> register(TenantRegisterRequest request) {
        Checks.notNull(request, "request");
        userOperator.registerWithPassword(new UserPasswordRegisterRequest(
                request.userName(),
                request.displayName(),
                request.email(),
                request.mobile(),
                request.region(),
                request.timeZone(),
                request.language(),
                request.encryptedPassword()));
        AuthLoginRequest loginRequest = new AuthLoginRequest(
                request.userName(),
                request.encryptedPassword(),
                null,
                null);
        return R.ok(tenantAuthFacade.login(loginRequest));
    }

    @POST
    @Path("/captcha/issue")
    public R<AuthCaptchaVo> issueLoginCaptcha(AuthCaptchaIssueRequest request) {
        String clientKey = request == null ? null : request.clientKey();
        return R.ok(tenantAuthFacade.issueLoginCaptcha(clientKey));
    }

    @POST
    @Path("/login")
    @Operation(operationId = "tenantAuthLogin", summary = "租户域登录")
    public R<AuthTokenVo> login(AuthLoginRequest request) {
        return R.ok(tenantAuthFacade.login(request));
    }

    @POST
    @Path("/select-tenant")
    public R<AuthTokenVo> selectTenant(SelectTenantRequest request) {
        String tenantUserId = requireAuthenticatedUserId();
        return R.ok(tenantAuthFacade.selectTenant(tenantUserId, request));
    }

    @POST
    @Path("/refresh")
    public R<AuthTokenVo> refresh(TokenRefreshRequest request) {
        return R.ok(tenantAuthFacade.refresh(request));
    }

    @POST
    @Path("/logout")
    public R<Void> logout() {
        tenantAuthFacade.logout();
        return R.ok();
    }

    @POST
    @Path("/password/change")
    public R<Void> changePassword(PasswordChangeRequest request) {
        Checks.notNull(request, "request");
        String userId = requireAuthenticatedUserId();
        String oldPassword = passwordDecryptor.decrypt(request.oldEncryptedPassword());
        String newPassword = passwordDecryptor.decrypt(request.newEncryptedPassword());
        passwordOperator.changePassword(userId, oldPassword, newPassword);
        return R.ok();
    }

    @POST
    @Path("/password/reset")
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

    private static String requireAuthenticatedUserId() {
        String userId = TLC.getString(TLC.USER_ID);
        if (userId == null || userId.isBlank()) {
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        return userId;
    }
}

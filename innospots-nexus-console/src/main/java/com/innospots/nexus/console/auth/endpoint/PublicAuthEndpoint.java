package com.innospots.nexus.console.auth.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.request.AuthCaptchaIssueRequest;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.auth.domain.vo.AuthRsaPublicKeyVo;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;

/**
 * 匿名认证 REST 契约（登录、验证码、RSA 公钥）。
 *
 * <p>类级 {@code @Path} 由业务域实现（如 platform）绑定。</p>
 */
@EndpointModule(value = "auth", ignoreAuth = true)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PublicAuth", description = "匿名认证")
public interface PublicAuthEndpoint {

    @GET
    @Path("/rsa-public-key")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "auth.rsa-public-key",
            label = @I18nLiteral(pairs = {"zh-CN", "获取登录密码 RSA 公钥", "en", "Get password RSA public key"}))
    @Operation(operationId = "authRsaPublicKey", summary = "获取登录密码 RSA 公钥")
    R<AuthRsaPublicKeyVo> getPasswordEncryptionPublicKey();

    @POST
    @Path("/captcha/issue")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "auth.captcha.issue",
            label = @I18nLiteral(pairs = {"zh-CN", "签发登录验证码", "en", "Issue login captcha"}),
            recordArgs = false)
    @Operation(operationId = "authCaptchaIssue", summary = "签发登录验证码")
    R<AuthCaptchaVo> issueLoginCaptcha(AuthCaptchaIssueRequest request);

    @POST
    @Path("/login")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "auth.login",
            label = @I18nLiteral(pairs = {"zh-CN", "密码登录", "en", "Password login"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "authLogin", summary = "密码登录")
    R<AuthTokenVo> login(AuthLoginRequest request);
}

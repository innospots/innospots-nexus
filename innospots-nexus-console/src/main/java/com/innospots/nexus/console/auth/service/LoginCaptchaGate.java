package com.innospots.nexus.console.auth.service;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthCaptchaVo;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.CaptchaIssueResult;
import com.innospots.nexus.console.credential.otp.domain.CaptchaVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;

/**
 * 登录前图形验证码策略与编排；实现委托 {@link CaptchaChallengeService}。
 */
@Slf4j
public final class LoginCaptchaGate {

    private final AuthConfig authConfig;
    private final CaptchaChallengeService captchaChallengeService;

    public LoginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        this.authConfig = Checks.notNull(authConfig, "authConfig");
        this.captchaChallengeService = Checks.notNull(captchaChallengeService, "captchaChallengeService");
    }

    /**
     * 当前安全域是否要求登录图形码。
     */
    public boolean isRequired(SecurityRealm realm) {
        Checks.notNull(realm, "realm");
        if (realm == SecurityRealm.PLATFORM) {
            return authConfig.isPlatformLoginCaptchaEnabled();
        }
        if (realm == SecurityRealm.TENANT) {
            return authConfig.isTenantLoginCaptchaEnabled();
        }
        return false;
    }

    /**
     * 发放登录用图形验证码。
     *
     * @param realm     平台或租户域
     * @param clientKey 客户端事务键；空白时自动生成
     */
    public AuthCaptchaVo issueForLogin(SecurityRealm realm, String clientKey) {
        Checks.notNull(realm, "realm");
        String resolvedClientKey = clientKey == null || clientKey.isBlank()
                ? UUID.randomUUID().toString()
                : clientKey;
        CaptchaIssueResult result = captchaChallengeService.issue(new CaptchaIssueCommand(
                realm,
                OtpPurpose.LOGIN_STEP_UP,
                resolvedClientKey,
                null));
        return AuthCaptchaVo.from(result, resolvedClientKey);
    }

    /**
     * 若策略开启则校验登录请求中的图形码；失败对外统一为认证失败。
     */
    public void verifyIfRequired(SecurityRealm realm, AuthLoginRequest request) {
        Checks.notNull(realm, "realm");
        Checks.notNull(request, "request");
        if (!isRequired(realm)) {
            return;
        }
        Checks.notBlank(request.captchaClientKey(), "captchaClientKey");
        Checks.notBlank(request.captchaCode(), "captchaCode");
        CaptchaVerifyCommand command = new CaptchaVerifyCommand(
                realm,
                OtpPurpose.LOGIN_STEP_UP,
                request.captchaClientKey(),
                request.captchaCode());
        if (!captchaChallengeService.verify(command)) {
            log.debug("Login captcha rejected for realm {}", realm);
            throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
        }
        captchaChallengeService.invalidate(command);
    }
}

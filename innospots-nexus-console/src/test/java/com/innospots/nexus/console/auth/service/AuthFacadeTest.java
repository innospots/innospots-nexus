package com.innospots.nexus.console.auth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithms;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class AuthFacadeTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void platformLoginIssuesBusinessTokenForMatchingPassword() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        AuthTokenVo token = harness.facade().login(loginRequest("ops", "Secret123"));

        assertThat(token.realm()).isEqualTo(SecurityRealm.PLATFORM);
        assertThat(token.tokenType()).isEqualTo("BUSINESS");
        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.tenantId()).isNull();
        TokenClaims claims = harness.issuer().parse(token.accessToken());
        assertThat(claims.userId()).isEqualTo("pus-ops");
        assertThat(claims.purpose()).isEqualTo("ACCESS");
        assertThat(TLC.userName()).isEqualTo("ops");
    }

    @Test
    void loginRejectsUnknownIdentity() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        assertThatThrownBy(() -> harness.facade().login(loginRequest("missing", "Secret123")))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        assertThatThrownBy(() -> harness.facade().login(loginRequest("ops", "Wrong123")))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.AUTHENTICATION_FAILED.fullCode());
    }

    @Test
    void refreshIssuesNewPairFromRefreshToken() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");
        AuthTokenVo original = harness.facade().login(loginRequest("ops", "Secret123"));

        AuthTokenVo refreshed = harness.facade().refresh(new TokenRefreshRequest(original.refreshToken()));

        assertThat(refreshed.accessToken()).isNotBlank().isNotEqualTo(original.accessToken());
        assertThat(refreshed.realm()).isEqualTo(SecurityRealm.PLATFORM);
    }

    @Test
    void logoutClearsSessionScope() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");
        harness.facade().login(loginRequest("ops", "Secret123"));

        harness.facade().logout();

        assertThat(TLC.userName()).isNull();
    }

    private static AuthLoginRequest loginRequest(String login, String password) {
        return new AuthLoginRequest(login, password, null, null);
    }

    private record AuthHarness(AuthFacade facade, TokenIssuer issuer) {

        private static AuthHarness platformUser(String login, String password) {
            InMemoryDirectory directory = new InMemoryDirectory();
            directory.addUser(new AuthUser("pus-" + login, login, "ACTIVE", SecurityRealm.PLATFORM));
            directory.addPassword("pus-" + login, password);
            return harness(directory);
        }

        private static AuthHarness harness(InMemoryDirectory directory) {
            AuthConfig authConfig = new AuthConfig();
            authConfig.setPlatformLoginCaptchaEnabled(false);
            LoginCaptchaGate captchaGate = new LoginCaptchaGate(authConfig, mock(CaptchaChallengeService.class));
            CredentialService credentialService = mock(CredentialService.class);
            stubAuthenticate(credentialService, directory);
            AuthConfig config = new AuthConfig();
            TokenIssuer issuer = new TokenIssuer(config);
            AuthTokenPairIssuer tokenPairIssuer = new AuthTokenPairIssuer(issuer);
            SessionScopeBinder sessionScopeBinder = new TestSessionScopeBinder();
            AuthFacade facade = new AuthFacade(
                    SecurityRealm.PLATFORM,
                    directory,
                    credentialService,
                    captchaGate,
                    encrypted -> encrypted,
                    issuer,
                    tokenPairIssuer,
                    sessionScopeBinder);
            return new AuthHarness(facade, issuer);
        }

        private static void stubAuthenticate(CredentialService credentialService, InMemoryDirectory directory) {
            doAnswer(invocation -> {
                String subjectId = invocation.getArgument(1);
                String rawPassword = invocation.getArgument(2);
                CredentialRecord record = directory.passwords.get(subjectId);
                if (record == null || !CryptoUtils.matchesPassword(rawPassword, record.verifier())) {
                    throw NexusException.build(NexusStatusCode.AUTHENTICATION_FAILED);
                }
                return null;
            }).when(credentialService).authenticate(eq(SecurityRealm.PLATFORM), anyString(), anyString());
        }
    }

    private static final class TestSessionScopeBinder implements SessionScopeBinder {

        @Override
        public void bindAfterAuth(AuthUser user, AuthSessionScope scope) {
            TLC.put(TLC.USER_ID, user.userId());
            TLC.put(TLC.USER_NAME, user.loginName());
        }

        @Override
        public void bindScope(AuthSessionScope scope) {
        }

        @Override
        public void bindFromClaims(TokenClaims claims) {
        }

        @Override
        public void clear() {
            TLC.clear();
        }
    }

    private static final class InMemoryDirectory implements UserDirectory {

        private final Map<String, AuthUser> usersByLogin = new HashMap<>();
        private final Map<String, AuthUser> usersById = new HashMap<>();
        private final Map<String, CredentialRecord> passwords = new HashMap<>();

        private void addUser(AuthUser user) {
            usersByLogin.put(user.realm() + ":" + user.loginName(), user);
            usersById.put(user.userId(), user);
        }

        private void addPassword(String userId, String rawPassword) {
            passwords.put(userId, new CredentialRecord(
                    userId,
                    CredentialKind.PASSWORD.name(),
                    CredentialAlgorithms.BCRYPT_V1,
                    CryptoUtils.encryptPassword(rawPassword),
                    null,
                    1,
                    0,
                    null,
                    false,
                    null));
        }

        @Override
        public Optional<AuthUser> findByLogin(String identity) {
            return usersByLogin.values().stream()
                    .filter(user -> identity.equals(user.loginName()))
                    .findFirst();
        }

        @Override
        public Optional<AuthUser> findById(String userId) {
            return Optional.ofNullable(usersById.get(userId));
        }
    }
}

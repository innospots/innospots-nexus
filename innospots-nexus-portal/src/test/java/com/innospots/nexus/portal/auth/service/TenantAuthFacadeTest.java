package com.innospots.nexus.portal.auth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.scope.TenantScope;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
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
import com.innospots.nexus.console.auth.service.AuthSessionScope;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.CredentialKind;
import com.innospots.nexus.console.credential.password.algorithm.CredentialAlgorithms;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.console.scope.service.SessionScopeTlc;
import com.innospots.nexus.portal.auth.api.MembershipDirectory;
import com.innospots.nexus.portal.auth.domain.model.TenantMembership;
import com.innospots.nexus.portal.auth.domain.request.SelectTenantRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

class TenantAuthFacadeTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void tenantLoginIssuesIdentityTokenWhenMultipleMembershipsExist() {
        AuthHarness harness = AuthHarness.tenantUser(
                "alice",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a"), new TenantMembership("tnt-b", "tmb-b")));

        AuthTokenVo token = harness.facade().login(loginRequest("alice", "Secret123"));

        assertThat(token.tokenType()).isEqualTo("IDENTITY");
        AuthTokenVo business = harness.facade().selectTenant("tus-alice", new SelectTenantRequest("tnt-b"));
        assertThat(business.tokenType()).isEqualTo("BUSINESS");
        assertThat(business.tenantId()).isEqualTo("tnt-b");
        assertThat(SessionContext.tenant()).isPresent();
    }

    @Test
    void tenantLoginIssuesBusinessTokenWhenExactlyOneMembershipExists() {
        AuthHarness harness = AuthHarness.tenantUser(
                "bob",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a")));

        AuthTokenVo token = harness.facade().login(loginRequest("bob", "Secret123"));

        assertThat(token.tokenType()).isEqualTo("BUSINESS");
        assertThat(token.tenantId()).isEqualTo("tnt-a");
    }

    @Test
    void logoutClearsSessionScope() {
        AuthHarness harness = AuthHarness.tenantUser(
                "bob",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a")));
        harness.facade().login(loginRequest("bob", "Secret123"));

        harness.facade().logout();

        assertThat(SessionContext.tenant()).isEmpty();
    }

    private static AuthLoginRequest loginRequest(String login, String password) {
        return new AuthLoginRequest(login, password, null, null);
    }

    private record AuthHarness(TenantAuthFacade facade, TokenIssuer issuer) {

        private static AuthHarness tenantUser(
                String login,
                String password,
                List<TenantMembership> memberships
        ) {
            InMemoryDirectory directory = new InMemoryDirectory();
            directory.addUser(new AuthUser("tus-" + login, login, "ACTIVE", SecurityRealm.TENANT));
            directory.addPassword("tus-" + login, password);
            directory.addMemberships("tus-" + login, memberships);
            return harness(directory);
        }

        private static AuthHarness harness(InMemoryDirectory directory) {
            AuthConfig authConfig = new AuthConfig();
            authConfig.setTenantLoginCaptchaEnabled(false);
            LoginCaptchaGate captchaGate = new LoginCaptchaGate(authConfig, mock(CaptchaChallengeService.class));
            CredentialService credentialService = mock(CredentialService.class);
            stubAuthenticate(credentialService, directory);
            AuthConfig config = new AuthConfig();
            TokenIssuer issuer = new TokenIssuer(config);
            AuthTokenPairIssuer tokenPairIssuer = new AuthTokenPairIssuer(issuer);
            SessionScopeBinder sessionScopeBinder = new TestSessionScopeBinder(directory);
            TenantAuthFacade facade = new TenantAuthFacade(
                    directory,
                    credentialService,
                    captchaGate,
                    directory,
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
            }).when(credentialService).authenticate(eq(SecurityRealm.TENANT), anyString(), anyString());
        }
    }

    private static final class TestSessionScopeBinder implements SessionScopeBinder {

        private final InMemoryDirectory directory;

        private TestSessionScopeBinder(InMemoryDirectory directory) {
            this.directory = directory;
        }

        @Override
        public void bindAfterAuth(AuthUser user, AuthSessionScope scope) {
            SessionScopeTlc.bindAuthUser(user);
            bindScope(scope);
        }

        @Override
        public void bindScope(AuthSessionScope scope) {
            SessionScopeTlc.applyScopeIds(scope);
            if (scope.tenantId() != null && !scope.tenantId().isBlank()) {
                directory.findByTenantId(scope.tenantId())
                        .ifPresent(tenantScope -> SessionContext.bindTenant(
                                tenantScope.tenant(), tenantScope.organization()));
            }
        }

        @Override
        public void bindFromClaims(TokenClaims claims) {
            SessionScopeTlc.applyClaimsIdentity(claims);
            bindScope(new AuthSessionScope(
                    claims.tokenType(),
                    claims.tenantId(),
                    claims.tenantMemberId(),
                    claims.workspaceId(),
                    claims.projectId()));
        }

        @Override
        public void clear() {
            SessionScopeTlc.clear();
        }
    }

    private static final class InMemoryDirectory implements UserDirectory, MembershipDirectory {

        private final Map<String, AuthUser> usersByLogin = new HashMap<>();
        private final Map<String, AuthUser> usersById = new HashMap<>();
        private final Map<String, CredentialRecord> passwords = new HashMap<>();
        private final Map<String, List<TenantMembership>> memberships = new HashMap<>();

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

        private void addMemberships(String tenantUserId, List<TenantMembership> values) {
            memberships.put(tenantUserId, List.copyOf(values));
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

        @Override
        public List<TenantMembership> listActiveMemberships(String tenantUserId) {
            return new ArrayList<>(memberships.getOrDefault(tenantUserId, List.of()));
        }

        public Optional<TenantScope> findByTenantId(String tenantId) {
            if (tenantId == null) {
                return Optional.empty();
            }
            TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
            OrganizationSnapshot organization = new OrganizationSnapshot(
                    tenantId, tenantId, tenantId, null, null, null, BasicStatus.ENABLED);
            return Optional.of(new TenantScope(tenant, organization));
        }
    }
}

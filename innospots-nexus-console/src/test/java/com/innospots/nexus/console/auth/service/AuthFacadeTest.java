package com.innospots.nexus.console.auth.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.organization.OrganizationSnapshot;
import com.innospots.nexus.base.domain.tenant.TenantSnapshot;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.SessionContext;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.CryptoUtils;
import com.innospots.nexus.console.auth.api.CredentialStore;
import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.api.UserDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.domain.model.AuthUser;
import com.innospots.nexus.console.auth.domain.model.CredentialRecord;
import com.innospots.nexus.console.auth.domain.model.TenantMembership;
import com.innospots.nexus.console.auth.domain.model.TokenClaims;
import com.innospots.nexus.console.auth.domain.request.AuthLoginRequest;
import com.innospots.nexus.console.auth.domain.request.SelectTenantRequest;
import com.innospots.nexus.console.auth.domain.request.TokenRefreshRequest;
import com.innospots.nexus.console.auth.domain.vo.AuthTokenVo;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.scope.api.ProjectScopeDirectory;
import com.innospots.nexus.console.scope.api.TenantScopeDirectory;
import com.innospots.nexus.console.scope.api.WorkspaceScopeDirectory;
import com.innospots.nexus.console.scope.domain.model.TenantScope;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthFacadeTest {

    @AfterEach
    void tearDown() {
        TLC.clear();
    }

    @Test
    void platformLoginIssuesBusinessTokenForMatchingPassword() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        AuthTokenVo token = harness.facade().login(
                SecurityRealm.PLATFORM, new AuthLoginRequest("ops", "Secret123"));

        assertThat(token.realm()).isEqualTo(SecurityRealm.PLATFORM);
        assertThat(token.tokenType()).isEqualTo("BUSINESS");
        assertThat(token.accessToken()).isNotBlank();
        assertThat(token.refreshToken()).isNotBlank();
        assertThat(token.tenantId()).isNull();
        assertThat(token.tenantMemberId()).isNull();
        assertThat(token.workspaceId()).isNull();
        assertThat(token.projectId()).isNull();
        TokenClaims claims = harness.issuer().parse(token.accessToken());
        assertThat(claims.userId()).isEqualTo("pus-ops");
        assertThat(claims.purpose()).isEqualTo("ACCESS");
        assertThat(TLC.userName()).isEqualTo("ops");
    }

    @Test
    void loginRejectsUnknownIdentity() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        assertThatThrownBy(() -> harness.facade().login(
                SecurityRealm.PLATFORM, new AuthLoginRequest("missing", "Secret123")))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.USER_NOT_FOUND.fullCode());
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");

        assertThatThrownBy(() -> harness.facade().login(
                SecurityRealm.PLATFORM, new AuthLoginRequest("ops", "Wrong123")))
                .isInstanceOf(NexusException.class)
                .extracting(error -> ((NexusException) error).code())
                .isEqualTo(NexusStatusCode.PASSWORD_ERROR.fullCode());
    }

    @Test
    void tenantLoginIssuesIdentityTokenWhenMultipleMembershipsExist() {
        AuthHarness harness = AuthHarness.tenantUser(
                "alice",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a"), new TenantMembership("tnt-b", "tmb-b")));

        AuthTokenVo token = harness.facade().login(
                SecurityRealm.TENANT, new AuthLoginRequest("alice", "Secret123"));

        assertThat(token.tokenType()).isEqualTo("IDENTITY");
        assertThat(token.tenantId()).isNull();
        AuthTokenVo business = harness.facade().selectTenant("tus-alice", new SelectTenantRequest("tnt-b"));
        assertThat(business.tokenType()).isEqualTo("BUSINESS");
        assertThat(business.tenantId()).isEqualTo("tnt-b");
        assertThat(business.tenantMemberId()).isEqualTo("tmb-b");
        assertThat(SessionContext.tenant()).isPresent();
        assertThat(SessionContext.organization()).isPresent();
    }

    @Test
    void tenantLoginIssuesBusinessTokenWhenExactlyOneMembershipExists() {
        AuthHarness harness = AuthHarness.tenantUser(
                "bob",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a")));

        AuthTokenVo token = harness.facade().login(
                SecurityRealm.TENANT, new AuthLoginRequest("bob", "Secret123"));

        assertThat(token.tokenType()).isEqualTo("BUSINESS");
        assertThat(token.tenantId()).isEqualTo("tnt-a");
        assertThat(token.tenantMemberId()).isEqualTo("tmb-a");
        assertThat(SessionContext.tenantId()).isEqualTo("tnt-a");
    }

    @Test
    void refreshIssuesNewPairFromRefreshToken() {
        AuthHarness harness = AuthHarness.platformUser("ops", "Secret123");
        AuthTokenVo original = harness.facade().login(
                SecurityRealm.PLATFORM, new AuthLoginRequest("ops", "Secret123"));

        AuthTokenVo refreshed = harness.facade().refresh(
                SecurityRealm.PLATFORM, new TokenRefreshRequest(original.refreshToken()));

        assertThat(refreshed.accessToken()).isNotBlank().isNotEqualTo(original.accessToken());
        assertThat(refreshed.refreshToken()).isNotBlank();
        assertThat(refreshed.realm()).isEqualTo(SecurityRealm.PLATFORM);
    }

    @Test
    void logoutClearsSessionScope() {
        AuthHarness harness = AuthHarness.tenantUser(
                "bob",
                "Secret123",
                List.of(new TenantMembership("tnt-a", "tmb-a")));
        harness.facade().login(SecurityRealm.TENANT, new AuthLoginRequest("bob", "Secret123"));

        harness.facade().logout();

        assertThat(SessionContext.tenant()).isEmpty();
        assertThat(TLC.tenantId()).isNull();
    }

    private record AuthHarness(AuthFacade facade, TokenIssuer issuer) {

        private static AuthHarness platformUser(String login, String password) {
            InMemoryDirectory directory = new InMemoryDirectory();
            directory.addUser(new AuthUser("pus-" + login, login, "ACTIVE", SecurityRealm.PLATFORM));
            directory.addPassword("pus-" + login, password);
            return harness(directory);
        }

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
            AuthConfig config = new AuthConfig();
            TokenIssuer issuer = new TokenIssuer(config);
            AuthTokenPairIssuer tokenPairIssuer = new AuthTokenPairIssuer(issuer);
            SessionScopeBinder sessionScopeBinder = new SessionScopeBinder(
                    directory, directory, directory);
            AuthFacade facade = new AuthFacade(
                    directory,
                    directory,
                    directory,
                    encrypted -> encrypted,
                    issuer,
                    tokenPairIssuer,
                    sessionScopeBinder);
            return new AuthHarness(facade, issuer);
        }
    }

    private static final class InMemoryDirectory
            implements UserDirectory, CredentialStore, MembershipDirectory,
            TenantScopeDirectory, WorkspaceScopeDirectory, ProjectScopeDirectory {

        private final Map<String, AuthUser> users = new HashMap<>();
        private final Map<String, CredentialRecord> passwords = new HashMap<>();
        private final Map<String, List<TenantMembership>> memberships = new HashMap<>();

        private void addUser(AuthUser user) {
            users.put(user.realm() + ":" + user.loginName(), user);
        }

        private void addPassword(String userId, String rawPassword) {
            String salt = CryptoUtils.generatePasswordSalt();
            passwords.put(userId, new CredentialRecord(
                    userId,
                    CryptoUtils.encryptPassword(rawPassword, salt),
                    salt,
                    "BCRYPT",
                    0,
                    null,
                    false));
        }

        private void addMemberships(String tenantUserId, List<TenantMembership> values) {
            memberships.put(tenantUserId, List.copyOf(values));
        }

        @Override
        public Optional<AuthUser> findByLogin(SecurityRealm realm, String identity) {
            return Optional.ofNullable(users.get(realm + ":" + identity));
        }

        @Override
        public Optional<CredentialRecord> findPassword(SecurityRealm realm, String userId) {
            return Optional.ofNullable(passwords.get(userId));
        }

        @Override
        public void updatePassword(SecurityRealm realm, CredentialRecord credential) {
            passwords.put(credential.userId(), credential);
        }

        @Override
        public List<TenantMembership> listActiveMemberships(String tenantUserId) {
            return new ArrayList<>(memberships.getOrDefault(tenantUserId, List.of()));
        }

        @Override
        public Optional<TenantScope> findByTenantId(String tenantId) {
            if (tenantId == null) {
                return Optional.empty();
            }
            TenantSnapshot tenant = new TenantSnapshot(tenantId, tenantId, tenantId, BasicStatus.ENABLED);
            OrganizationSnapshot organization = new OrganizationSnapshot(
                    tenantId, tenantId, tenantId, null, null, null, BasicStatus.ENABLED);
            return Optional.of(new TenantScope(tenant, organization));
        }

        @Override
        public Optional<com.innospots.nexus.base.domain.workspace.WorkspaceSnapshot> findWorkspace(
                String tenantId,
                String workspaceId
        ) {
            return Optional.empty();
        }

        @Override
        public Optional<com.innospots.nexus.base.domain.project.ProjectSnapshot> findProject(
                String tenantId,
                String workspaceId,
                String projectId
        ) {
            return Optional.empty();
        }
    }
}

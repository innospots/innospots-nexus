package com.innospots.nexus.quarkus.portal.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.portal.auth.adapter.PortalMembershipDirectory;
import com.innospots.nexus.portal.auth.adapter.PortalUserDirectory;
import com.innospots.nexus.portal.auth.service.TenantAuthFacade;
import com.innospots.nexus.portal.member.dao.TenantMemberDao;
import com.innospots.nexus.portal.organization.dao.OrganizationUnitDao;
import com.innospots.nexus.portal.project.dao.ProjectDao;
import com.innospots.nexus.portal.project.operator.ProjectOperator;
import com.innospots.nexus.portal.scope.PortalProjectScopeDirectory;
import com.innospots.nexus.portal.scope.PortalTenantScopeDirectory;
import com.innospots.nexus.portal.scope.PortalWorkspaceScopeDirectory;
import com.innospots.nexus.portal.scope.service.PortalSessionScopeBinder;
import com.innospots.nexus.portal.user.dao.UserDao;
import com.innospots.nexus.portal.user.operator.PasswordOperator;
import com.innospots.nexus.portal.user.operator.UserOperator;
import com.innospots.nexus.portal.workspace.dao.WorkspaceDao;
import com.innospots.nexus.portal.workspace.operator.WorkspaceOperator;

/**
 * portal 租户认证、作用域与用户相关 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PortalAuthBeans {

    @Produces
    @Singleton
    PortalUserDirectory portalUserDirectory(UserDao userDao) {
        return new PortalUserDirectory(userDao);
    }

    @Produces
    @Singleton
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Produces
    @Singleton
    PortalMembershipDirectory portalMembershipDirectory(TenantMemberDao tenantMemberDao) {
        return new PortalMembershipDirectory(tenantMemberDao);
    }

    @Produces
    @Singleton
    WorkspaceOperator workspaceOperator(WorkspaceDao workspaceDao) {
        return new WorkspaceOperator(workspaceDao);
    }

    @Produces
    @Singleton
    ProjectOperator projectOperator(ProjectDao projectDao) {
        return new ProjectOperator(projectDao);
    }

    @Produces
    @Singleton
    PortalTenantScopeDirectory tenantScopeDirectory(OrganizationUnitDao organizationUnitDao) {
        return new PortalTenantScopeDirectory(organizationUnitDao);
    }

    @Produces
    @Singleton
    PortalWorkspaceScopeDirectory workspaceScopeDirectory(WorkspaceOperator workspaceOperator) {
        return new PortalWorkspaceScopeDirectory(workspaceOperator);
    }

    @Produces
    @Singleton
    PortalProjectScopeDirectory projectScopeDirectory(ProjectOperator projectOperator) {
        return new PortalProjectScopeDirectory(projectOperator);
    }

    @Produces
    @Singleton
    SessionScopeBinder sessionScopeBinder(
            PortalTenantScopeDirectory tenantScopeDirectory,
            PortalWorkspaceScopeDirectory workspaceScopeDirectory,
            PortalProjectScopeDirectory projectScopeDirectory) {
        return new PortalSessionScopeBinder(tenantScopeDirectory, workspaceScopeDirectory, projectScopeDirectory);
    }

    @Produces
    @Singleton
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Produces
    @Singleton
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    @Produces
    @Singleton
    TenantAuthFacade tenantAuthFacade(
            PortalUserDirectory portalUserDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            PortalMembershipDirectory membershipDirectory,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new TenantAuthFacade(
                portalUserDirectory,
                credentialService,
                loginCaptchaGate,
                membershipDirectory,
                passwordDecryptor,
                tokenIssuer,
                authTokenPairIssuer,
                sessionScopeBinder);
    }

    @Produces
    @Singleton
    UserOperator userOperator(
            UserDao userDao,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor) {
        return new UserOperator(userDao, credentialService, passwordDecryptor);
    }

    @Produces
    @Singleton
    PasswordOperator passwordOperator(
            UserDao userDao,
            CredentialService credentialService,
            com.innospots.nexus.console.credential.password.PasswordVerificationOperator passwordVerificationOperator) {
        return new PasswordOperator(userDao, credentialService, passwordVerificationOperator);
    }
}

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
import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeResolver;
import com.innospots.nexus.console.jaxrs.filter.WorkspaceScopeSessionFilter;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;
import com.innospots.nexus.console.scope.cookie.WorkspaceCookieNames;
import com.innospots.nexus.console.scope.domain.session.SessionScopeBinder;
import com.innospots.nexus.console.scope.service.ConsoleTenantContextService;
import com.innospots.nexus.console.scope.service.DefaultSessionScopeBinder;
import com.innospots.nexus.console.scope.service.ConsoleWorkspaceContextService;
import com.innospots.nexus.console.scope.service.ConsoleWorkspaceScopeResolver;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceMemberDao;
import com.innospots.nexus.portal.auth.adapter.PortalUserDirectory;
import com.innospots.nexus.portal.auth.service.TenantAuthFacade;
import com.innospots.nexus.console.scope.tenant.dao.TenantMemberDao;
import com.innospots.nexus.console.scope.tenant.dao.TenantDao;
import com.innospots.nexus.console.scope.project.dao.ProjectDao;
import com.innospots.nexus.console.scope.project.operator.ProjectOperator;
import com.innospots.nexus.console.scope.directory.PortalProjectScopeDirectory;
import com.innospots.nexus.console.scope.directory.PortalTenantScopeDirectory;
import com.innospots.nexus.console.scope.directory.PortalWorkspaceScopeDirectory;
import com.innospots.nexus.console.user.portal.dao.PortalUserDao;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.user.portal.dao.PortalUserOauthIdentityDao;
import com.innospots.nexus.console.user.portal.operator.PortalUserOauthOperator;
import com.innospots.nexus.console.user.portal.service.PortalPasswordService;
import com.innospots.nexus.console.user.portal.service.PortalUserService;
import com.innospots.nexus.console.scope.workspace.dao.WorkspaceDao;
import com.innospots.nexus.console.scope.workspace.operator.WorkspaceOperator;

/**
 * portal 租户认证、作用域与用户相关 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PortalAuthBeans {

    @Produces
    @Singleton
    PortalUserDirectory portalUserDirectory(PortalUserDao userDao) {
        return new PortalUserDirectory(userDao);
    }

    @Produces
    @Singleton
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
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
    PortalTenantScopeDirectory tenantScopeDirectory(TenantDao tenantDao) {
        return new PortalTenantScopeDirectory(tenantDao);
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
    SessionScopeBinder sessionScopeBinder(PortalTenantScopeDirectory tenantScopeDirectory) {
        return new DefaultSessionScopeBinder(tenantScopeDirectory);
    }


    @Produces
    @Singleton
    ConsoleTenantContextService consoleTenantContextService(
            TenantDao tenantDao,
            TenantMemberDao tenantMemberDao,
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new ConsoleTenantContextService(
                tenantDao, tenantMemberDao, roleBindingDao, roleDao);
    }

    @Produces
    @Singleton
    ConsoleWorkspaceContextService consoleWorkspaceContextService(
            WorkspaceOperator workspaceOperator,
            WorkspaceMemberDao workspaceMemberDao,
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new ConsoleWorkspaceContextService(
                workspaceOperator, workspaceMemberDao, roleBindingDao, roleDao);
    }

    @Produces
    @Singleton
    WorkspaceScopeResolver workspaceScopeResolver(
            ConsoleTenantContextService tenantContextService,
            ConsoleWorkspaceContextService workspaceContextService,
            ProjectOperator projectOperator) {
        return new ConsoleWorkspaceScopeResolver(
                tenantContextService,
                workspaceContextService,
                projectOperator::findSnapshot,
                new ConsoleWorkspaceScopeResolver.Names(
                        WorkspaceCookieNames.TENANT,
                        WorkspaceCookieNames.WORKSPACE,
                        WorkspaceCookieNames.PROJECT));
    }

    @Produces
    @Singleton
    WorkspaceScopeSessionFilter workspaceScopeSessionFilter(
            WorkspaceScopeResolver workspaceScopeResolver) {
        return new WorkspaceScopeSessionFilter(workspaceScopeResolver);
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
            TenantMemberDao tenantMemberDao,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new TenantAuthFacade(
                portalUserDirectory,
                credentialService,
                loginCaptchaGate,
                tenantMemberDao,
                passwordDecryptor,
                tokenIssuer,
                authTokenPairIssuer,
                sessionScopeBinder);
    }

    @Produces
    @Singleton
    PortalUserService portalUserService(
            PortalUserDao portalUserDao,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor) {
        return new PortalUserService(portalUserDao, credentialService, passwordDecryptor);
    }

    @Produces
    @Singleton
    PortalPasswordService portalPasswordService(
            PortalUserDao portalUserDao,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator) {
        return new PortalPasswordService(
                portalUserDao, credentialService, passwordVerificationOperator);
    }

    @Produces
    @Singleton
    PortalUserOauthOperator portalUserOauthOperator(
            PortalUserDao portalUserDao,
            PortalUserOauthIdentityDao portalUserOauthIdentityDao) {
        return new PortalUserOauthOperator(portalUserDao, portalUserOauthIdentityDao);
    }
}

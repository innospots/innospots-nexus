package com.innospots.nexus.spring.portal.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.portal.auth.adapter.PortalMembershipDirectory;
import com.innospots.nexus.portal.auth.adapter.PortalUserDirectory;
import com.innospots.nexus.portal.auth.api.MembershipDirectory;
import com.innospots.nexus.portal.auth.endpoint.TenantAuthEndpoint;
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
 * portal 租户认证、作用域与用户相关 Spring 装配。
 *
 * @author Smars
 * @date 2026/09/23
 * @see TenantAuthEndpoint
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.portal.user.dao",
            "com.innospots.nexus.portal.member.dao",
            "com.innospots.nexus.portal.workspace.dao",
            "com.innospots.nexus.portal.project.dao",
            "com.innospots.nexus.portal.organization.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PortalAuthConfiguration {

    @Bean
    PortalUserDirectory portalUserDirectory(UserDao userDao) {
        return new PortalUserDirectory(userDao);
    }

    @Bean
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Bean
    PortalMembershipDirectory portalMembershipDirectory(TenantMemberDao tenantMemberDao) {
        return new PortalMembershipDirectory(tenantMemberDao);
    }

    @Bean
    MembershipDirectory membershipDirectory(PortalMembershipDirectory portalMembershipDirectory) {
        return portalMembershipDirectory;
    }

    @Bean
    WorkspaceOperator workspaceOperator(WorkspaceDao workspaceDao) {
        return new WorkspaceOperator(workspaceDao);
    }

    @Bean
    ProjectOperator projectOperator(ProjectDao projectDao) {
        return new ProjectOperator(projectDao);
    }

    @Bean
    PortalTenantScopeDirectory tenantScopeDirectory(OrganizationUnitDao organizationUnitDao) {
        return new PortalTenantScopeDirectory(organizationUnitDao);
    }

    @Bean
    PortalWorkspaceScopeDirectory workspaceScopeDirectory(WorkspaceOperator workspaceOperator) {
        return new PortalWorkspaceScopeDirectory(workspaceOperator);
    }

    @Bean
    PortalProjectScopeDirectory projectScopeDirectory(ProjectOperator projectOperator) {
        return new PortalProjectScopeDirectory(projectOperator);
    }

    @Bean
    SessionScopeBinder sessionScopeBinder(
            PortalTenantScopeDirectory tenantScopeDirectory,
            PortalWorkspaceScopeDirectory workspaceScopeDirectory,
            PortalProjectScopeDirectory projectScopeDirectory) {
        return new PortalSessionScopeBinder(tenantScopeDirectory, workspaceScopeDirectory, projectScopeDirectory);
    }

    @Bean
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Bean
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    @Bean
    TenantAuthFacade tenantAuthFacade(
            PortalUserDirectory portalUserDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            MembershipDirectory membershipDirectory,
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

    @Bean
    UserOperator userOperator(
            UserDao userDao,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor) {
        return new UserOperator(userDao, credentialService, passwordDecryptor);
    }

    @Bean
    PasswordOperator passwordOperator(
            UserDao userDao,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator) {
        return new PasswordOperator(userDao, credentialService, passwordVerificationOperator);
    }

    @Bean
    @Lazy
    TenantAuthEndpoint tenantAuthEndpoint(
            TenantAuthFacade tenantAuthFacade,
            UserOperator userOperator,
            PasswordOperator passwordOperator,
            PasswordDecryptor passwordDecryptor) {
        return new TenantAuthEndpoint(tenantAuthFacade, userOperator, passwordOperator, passwordDecryptor);
    }
}

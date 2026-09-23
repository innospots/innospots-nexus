package com.innospots.nexus.sample.spring.tenant.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.auth.api.MembershipDirectory;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.auth.service.AuthFacade;
import com.innospots.nexus.console.auth.service.AuthTokenPairIssuer;
import com.innospots.nexus.console.auth.service.LoginCaptchaGate;
import com.innospots.nexus.console.auth.service.TokenIssuer;
import com.innospots.nexus.console.config.AuthConfig;
import com.innospots.nexus.console.credential.otp.service.CaptchaChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.RsaPasswordDecryptor;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.scope.service.SessionScopeBinder;
import com.innospots.nexus.kernel.scope.service.KernelSessionScopeBinder;
import com.innospots.nexus.kernel.auth.adapter.KernelMembershipDirectory;
import com.innospots.nexus.kernel.auth.adapter.KernelUserDirectory;
import com.innospots.nexus.kernel.auth.endpoint.TenantAuthEndpoint;
import com.innospots.nexus.kernel.member.dao.TenantMemberDao;
import com.innospots.nexus.kernel.project.dao.ProjectDao;
import com.innospots.nexus.kernel.project.operator.ProjectOperator;
import com.innospots.nexus.kernel.organization.dao.OrganizationUnitDao;
import com.innospots.nexus.kernel.scope.KernelProjectScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelTenantScopeDirectory;
import com.innospots.nexus.kernel.scope.KernelWorkspaceScopeDirectory;
import com.innospots.nexus.kernel.user.dao.UserDao;
import com.innospots.nexus.kernel.user.operator.PasswordOperator;
import com.innospots.nexus.kernel.user.operator.UserOperator;
import com.innospots.nexus.kernel.workspace.dao.WorkspaceDao;
import com.innospots.nexus.kernel.workspace.operator.WorkspaceOperator;

/**
 * 租户示例：{@code console.auth} 与 kernel 租户认证端点装配。
 */
@Configuration
@MapperScan(
        basePackages = {
            "com.innospots.nexus.kernel.user.dao",
            "com.innospots.nexus.kernel.member.dao",
            "com.innospots.nexus.kernel.workspace.dao",
            "com.innospots.nexus.kernel.project.dao",
            "com.innospots.nexus.kernel.organization.dao"
        },
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class TenantSampleConsoleAuthConfiguration {

    @Bean
    AuthConfig authConfig(
            @Value("${nexus.console.auth.token-secret:}") String tokenSecret,
            @Value("${nexus.console.auth.access-token-ttl-seconds:7200}") long accessTtl,
            @Value("${nexus.console.auth.refresh-token-ttl-seconds:604800}") long refreshTtl,
            @Value("${nexus.console.auth.tenant-login-captcha-enabled:true}") boolean tenantLoginCaptchaEnabled,
            @Value("${nexus.console.auth.platform-login-captcha-enabled:true}") boolean platformLoginCaptchaEnabled) {
        AuthConfig config = new AuthConfig();
        if (tokenSecret != null && !tokenSecret.isBlank()) {
            config.setTokenSecret(tokenSecret);
        }
        config.setAccessTokenTtlSeconds(accessTtl);
        config.setRefreshTokenTtlSeconds(refreshTtl);
        config.setTenantLoginCaptchaEnabled(tenantLoginCaptchaEnabled);
        config.setPlatformLoginCaptchaEnabled(platformLoginCaptchaEnabled);
        return config;
    }

    @Bean
    PasswordDecryptor passwordDecryptor(
            @Value("${nexus.console.auth.rsa.private-key}") String privateKey) {
        return new RsaPasswordDecryptor(privateKey);
    }

    @Bean
    KernelUserDirectory kernelUserDirectory(UserDao userDao) {
        return new KernelUserDirectory(userDao);
    }

    @Bean
    LoginCaptchaGate loginCaptchaGate(AuthConfig authConfig, CaptchaChallengeService captchaChallengeService) {
        return new LoginCaptchaGate(authConfig, captchaChallengeService);
    }

    @Bean
    KernelMembershipDirectory kernelMembershipDirectory(TenantMemberDao tenantMemberDao) {
        return new KernelMembershipDirectory(tenantMemberDao);
    }

    @Bean
    MembershipDirectory membershipDirectory(KernelMembershipDirectory kernelMembershipDirectory) {
        return kernelMembershipDirectory;
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
    KernelTenantScopeDirectory tenantScopeDirectory(OrganizationUnitDao organizationUnitDao) {
        return new KernelTenantScopeDirectory(organizationUnitDao);
    }

    @Bean
    KernelWorkspaceScopeDirectory workspaceScopeDirectory(WorkspaceOperator workspaceOperator) {
        return new KernelWorkspaceScopeDirectory(workspaceOperator);
    }

    @Bean
    KernelProjectScopeDirectory projectScopeDirectory(ProjectOperator projectOperator) {
        return new KernelProjectScopeDirectory(projectOperator);
    }

    @Bean
    SessionScopeBinder sessionScopeBinder(
            KernelTenantScopeDirectory tenantScopeDirectory,
            KernelWorkspaceScopeDirectory workspaceScopeDirectory,
            KernelProjectScopeDirectory projectScopeDirectory) {
        return new KernelSessionScopeBinder(tenantScopeDirectory, workspaceScopeDirectory, projectScopeDirectory);
    }

    @Bean
    TokenIssuer tokenIssuer(AuthConfig authConfig) {
        return new TokenIssuer(authConfig);
    }

    @Bean
    AuthTokenPairIssuer authTokenPairIssuer(TokenIssuer tokenIssuer) {
        return new AuthTokenPairIssuer(tokenIssuer);
    }

    @Bean(name = "tenantAuthFacade")
    AuthFacade tenantAuthFacade(
            KernelUserDirectory kernelUserDirectory,
            CredentialService credentialService,
            LoginCaptchaGate loginCaptchaGate,
            MembershipDirectory membershipDirectory,
            PasswordDecryptor passwordDecryptor,
            TokenIssuer tokenIssuer,
            AuthTokenPairIssuer authTokenPairIssuer,
            SessionScopeBinder sessionScopeBinder) {
        return new AuthFacade(
                SecurityRealm.TENANT,
                kernelUserDirectory,
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
            @Qualifier("tenantAuthFacade") AuthFacade authFacade,
            UserOperator userOperator,
            PasswordOperator passwordOperator,
            PasswordDecryptor passwordDecryptor) {
        return new TenantAuthEndpoint(authFacade, userOperator, passwordOperator, passwordDecryptor);
    }
}

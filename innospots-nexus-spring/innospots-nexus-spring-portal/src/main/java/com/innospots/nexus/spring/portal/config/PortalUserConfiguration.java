package com.innospots.nexus.spring.portal.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.console.credential.password.PasswordVerificationOperator;
import com.innospots.nexus.console.credential.password.service.CredentialService;
import com.innospots.nexus.console.user.portal.dao.PortalUserDao;
import com.innospots.nexus.console.user.portal.dao.PortalUserOauthIdentityDao;
import com.innospots.nexus.console.user.portal.operator.PortalUserOauthOperator;
import com.innospots.nexus.console.user.portal.service.PortalPasswordService;
import com.innospots.nexus.console.user.portal.service.PortalUserService;
import com.innospots.nexus.portal.auth.adapter.PortalUserDirectory;

/**
 * portal 平台注册用户的 Spring 装配。
 *
 * <p>只装配用户领域自身的能力：用户目录、用户服务、密码服务与 OAuth 身份绑定。
 * 登录编排见 {@link PortalAuthConfiguration}。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.user.portal.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
@ConditionalOnProperty(prefix = "nexus.console.auth.rsa", name = "private-key")
public class PortalUserConfiguration {

    /**
     * 登录身份目录：按登录名、邮箱或手机号定位平台注册用户。
     *
     * @param portalUserDao 平台用户 DAO
     * @return 用户目录
     */
    @Bean
    PortalUserDirectory portalUserDirectory(PortalUserDao portalUserDao) {
        return new PortalUserDirectory(portalUserDao);
    }

    /**
     * 平台注册用户分页与注册。
     *
     * <p>依赖凭据域，属于跨领域编排，因此放在 service 而非 operator。</p>
     *
     * @param portalUserDao     平台用户 DAO
     * @param credentialService 凭证服务
     * @param passwordDecryptor 密码解密器
     * @return 用户服务
     */
    @Bean
    PortalUserService portalUserService(
            PortalUserDao portalUserDao,
            CredentialService credentialService,
            PasswordDecryptor passwordDecryptor) {
        return new PortalUserService(portalUserDao, credentialService, passwordDecryptor);
    }

    /**
     * 平台注册用户密码变更与重置。
     *
     * @param portalUserDao                平台用户 DAO
     * @param credentialService            凭证服务
     * @param passwordVerificationOperator 密码校验操作器
     * @return 密码服务
     */
    @Bean
    PortalPasswordService portalPasswordService(
            PortalUserDao portalUserDao,
            CredentialService credentialService,
            PasswordVerificationOperator passwordVerificationOperator) {
        return new PortalPasswordService(
                portalUserDao, credentialService, passwordVerificationOperator);
    }

    /**
     * 平台注册用户的 OAuth 身份绑定。
     *
     * <p>只依赖本域两个 DAO，属于纯持久化，因此保留在 operator 层。</p>
     *
     * @param portalUserDao             平台用户 DAO
     * @param portalUserOauthIdentityDao OAuth 身份 DAO
     * @return OAuth 用户操作器
     */
    @Bean
    PortalUserOauthOperator portalUserOauthOperator(
            PortalUserDao portalUserDao,
            PortalUserOauthIdentityDao portalUserOauthIdentityDao) {
        return new PortalUserOauthOperator(portalUserDao, portalUserOauthIdentityDao);
    }
}

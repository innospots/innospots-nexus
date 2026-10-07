package com.innospots.nexus.spring.platform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.dao.PlatformAccessRequestDao;
import com.innospots.nexus.platform.access.endpoint.PlatformAccessRequestEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicAccessRegistrationEndpoint;
import com.innospots.nexus.platform.access.endpoint.PlatformPublicOpenRegistrationEndpoint;
import com.innospots.nexus.platform.access.operator.PlatformAccessRequestOperator;
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService;
import com.innospots.nexus.platform.invite.dao.PlatformInviteDao;
import com.innospots.nexus.platform.invite.endpoint.PlatformInviteEndpoint;
import com.innospots.nexus.platform.invite.endpoint.PlatformPublicInviteEndpoint;
import com.innospots.nexus.platform.invite.operator.PlatformInviteOperator;
import com.innospots.nexus.platform.invite.service.PlatformInviteService;
import com.innospots.nexus.platform.invite.service.PlatformPublicInviteService;
import com.innospots.nexus.platform.invite.support.PlatformInviteLinkBuilder;
import com.innospots.nexus.platform.invite.support.PlatformInviteOtpNotifier;
import com.innospots.nexus.platform.user.service.PlatformUserService;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

@Configuration
public class PlatformProvisioningConfiguration {

    @Bean
    PlatformInviteLinkBuilder platformInviteLinkBuilder(
            @Value("${nexus.platform.invite.public-base-url:}") String publicBaseUrl) {
        return new PlatformInviteLinkBuilder(publicBaseUrl);
    }

    @Bean
    PlatformInviteOtpNotifier platformInviteOtpNotifier(OtpChallengeService otpChallengeService) {
        return new PlatformInviteOtpNotifier(otpChallengeService);
    }

    @Bean
    PlatformInviteOperator platformInviteOperator(PlatformInviteDao inviteDao) {
        return new PlatformInviteOperator(inviteDao);
    }

    @Bean
    PlatformInviteService platformInviteService(
            PlatformInviteOperator platformInviteOperator,
            PlatformInviteOtpNotifier inviteOtpNotifier,
            PlatformInviteLinkBuilder inviteLinkBuilder) {
        return new PlatformInviteService(platformInviteOperator, inviteOtpNotifier, inviteLinkBuilder);
    }

    @Bean
    PlatformPublicInviteService platformPublicInviteService(
            PlatformInviteOperator platformInviteOperator,
            PlatformUserService platformUserService,
            OtpChallengeService otpChallengeService,
            PasswordDecryptor passwordDecryptor,
            PlatformRegistrationModeSettingService registrationModeSettingService,
            PlatformUserRoleProvisioner platformUserRoleProvisioner) {
        return new PlatformPublicInviteService(
                platformInviteOperator,
                platformUserService,
                otpChallengeService,
                passwordDecryptor,
                registrationModeSettingService,
                platformUserRoleProvisioner);
    }

    @Bean
    PlatformAccessRequestOperator platformAccessRequestOperator(PlatformAccessRequestDao accessRequestDao) {
        return new PlatformAccessRequestOperator(accessRequestDao);
    }

    @Bean
    PlatformAccessRequestService platformAccessRequestService(
            PlatformAccessRequestOperator platformAccessRequestOperator,
            PlatformUserService platformUserService,
            OtpChallengeService otpChallengeService,
            PasswordDecryptor passwordDecryptor,
            PlatformRegistrationModeSettingService registrationModeSettingService) {
        return new PlatformAccessRequestService(
                platformAccessRequestOperator,
                platformUserService,
                otpChallengeService,
                passwordDecryptor,
                registrationModeSettingService);
    }

    @Bean
    PlatformOpenRegistrationService platformOpenRegistrationService(
            PlatformUserService platformUserService,
            OtpChallengeService otpChallengeService,
            PasswordDecryptor passwordDecryptor,
            PlatformRegistrationModeSettingService registrationModeSettingService) {
        return new PlatformOpenRegistrationService(
                platformUserService,
                otpChallengeService,
                passwordDecryptor,
                registrationModeSettingService);
    }

    @Bean
    @Lazy
    PlatformInviteEndpoint platformInviteEndpoint(PlatformInviteService platformInviteService) {
        return new PlatformInviteEndpoint(platformInviteService);
    }

    @Bean
    @Lazy
    PlatformPublicInviteEndpoint platformPublicInviteEndpoint(PlatformPublicInviteService publicInviteService) {
        return new PlatformPublicInviteEndpoint(publicInviteService);
    }

    @Bean
    @Lazy
    PlatformAccessRequestEndpoint platformAccessRequestEndpoint(
            PlatformAccessRequestService platformAccessRequestService) {
        return new PlatformAccessRequestEndpoint(platformAccessRequestService);
    }

    @Bean
    @Lazy
    PlatformPublicAccessRegistrationEndpoint platformPublicAccessRegistrationEndpoint(
            PlatformAccessRequestService platformAccessRequestService) {
        return new PlatformPublicAccessRegistrationEndpoint(platformAccessRequestService);
    }

    @Bean
    @Lazy
    PlatformPublicOpenRegistrationEndpoint platformPublicOpenRegistrationEndpoint(
            PlatformOpenRegistrationService openRegistrationService) {
        return new PlatformPublicOpenRegistrationEndpoint(openRegistrationService);
    }
}

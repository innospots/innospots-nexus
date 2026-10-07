package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.dao.PlatformAccessRequestDao;
import com.innospots.nexus.platform.access.operator.PlatformAccessRequestOperator;
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService;
import com.innospots.nexus.platform.invite.dao.PlatformInviteDao;
import com.innospots.nexus.platform.invite.operator.PlatformInviteOperator;
import com.innospots.nexus.platform.invite.service.PlatformInviteService;
import com.innospots.nexus.platform.invite.service.PlatformPublicInviteService;
import com.innospots.nexus.platform.invite.support.PlatformInviteLinkBuilder;
import com.innospots.nexus.platform.invite.support.PlatformInviteOtpNotifier;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.user.service.PlatformUserService;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

@ApplicationScoped
public class PlatformProvisioningBeans {

    @Produces
    @Singleton
    PlatformInviteLinkBuilder platformInviteLinkBuilder(
            @ConfigProperty(name = "nexus.platform.invite.public-base-url", defaultValue = "") String publicBaseUrl) {
        return new PlatformInviteLinkBuilder(publicBaseUrl);
    }

    @Produces
    @Singleton
    PlatformInviteOtpNotifier platformInviteOtpNotifier(OtpChallengeService otpChallengeService) {
        return new PlatformInviteOtpNotifier(otpChallengeService);
    }

    @Produces
    @Singleton
    PlatformInviteOperator platformInviteOperator(PlatformInviteDao inviteDao) {
        return new PlatformInviteOperator(inviteDao);
    }

    @Produces
    @Singleton
    PlatformInviteService platformInviteService(
            PlatformInviteOperator platformInviteOperator,
            PlatformInviteOtpNotifier inviteOtpNotifier,
            PlatformInviteLinkBuilder inviteLinkBuilder) {
        return new PlatformInviteService(platformInviteOperator, inviteOtpNotifier, inviteLinkBuilder);
    }

    @Produces
    @Singleton
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

    @Produces
    @Singleton
    PlatformAccessRequestOperator platformAccessRequestOperator(PlatformAccessRequestDao accessRequestDao) {
        return new PlatformAccessRequestOperator(accessRequestDao);
    }

    @Produces
    @Singleton
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

    @Produces
    @Singleton
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
}

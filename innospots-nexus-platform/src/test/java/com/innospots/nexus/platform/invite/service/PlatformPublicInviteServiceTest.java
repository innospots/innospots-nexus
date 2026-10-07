package com.innospots.nexus.platform.invite.service;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.credential.otp.domain.OtpVerifyCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.invite.domain.entity.PlatformInviteEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteStatus;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteAcceptRequest;
import com.innospots.nexus.platform.invite.domain.request.PlatformInviteActivateByCodeRequest;
import com.innospots.nexus.platform.invite.operator.PlatformInviteOperator;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;
import com.innospots.nexus.platform.settings.domain.enums.PlatformRegistrationMode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.settings.status.PlatformRegistrationModeSettingStatusCode;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;
import com.innospots.nexus.platform.user.support.PlatformUserRoleProvisioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlatformPublicInviteServiceTest {

    @Test
    void acceptInviteOfflineSkipsOtpAndAssignsRoles() {
        PlatformInviteOperator inviteOperator = mock(PlatformInviteOperator.class);
        PlatformUserService userService = mock(PlatformUserService.class);
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        PasswordDecryptor passwordDecryptor = mock(PasswordDecryptor.class);
        PlatformRegistrationModeSettingService modeService = mock(PlatformRegistrationModeSettingService.class);
        PlatformUserRoleProvisioner roleProvisioner = mock(PlatformUserRoleProvisioner.class);

        PlatformInviteEntity invite = pendingInvite(PlatformInviteDeliveryMode.OFFLINE, "ops,viewer");
        when(inviteOperator.requirePendingByToken("tok")).thenReturn(invite);
        when(passwordDecryptor.decrypt("enc")).thenReturn("raw-pass");
        when(userService.createActiveUserWithPassword(
                eq("alice"),
                eq("alice@example.com"),
                eq(null),
                eq("raw-pass")))
                .thenReturn(new PlatformUserVo(
                        "usr-1",
                        "alice",
                        null,
                        "alice@example.com",
                        null,
                        null,
                        PlatformUserStatus.ACTIVE.name(),
                        null,
                        null,
                        null));

        PlatformPublicInviteService service = new PlatformPublicInviteService(
                inviteOperator,
                userService,
                otpChallengeService,
                passwordDecryptor,
                modeService,
                roleProvisioner);

        String userId = service.acceptInvite("tok", new PlatformInviteAcceptRequest(null, "enc", null, null));

        assertThat(userId).isEqualTo("usr-1");
        verify(otpChallengeService, never()).verifyOrThrow(any(OtpVerifyCommand.class));
        verify(roleProvisioner).assignDefaultRolesIfPresent("usr-1", "ops,viewer");
        verify(inviteOperator).markAccepted(invite, "usr-1");
    }

    @Test
    void acceptInviteOnlineRequiresOtpVerification() {
        PlatformInviteOperator inviteOperator = mock(PlatformInviteOperator.class);
        PlatformUserService userService = mock(PlatformUserService.class);
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        PasswordDecryptor passwordDecryptor = mock(PasswordDecryptor.class);
        PlatformRegistrationModeSettingService modeService = mock(PlatformRegistrationModeSettingService.class);
        PlatformUserRoleProvisioner roleProvisioner = mock(PlatformUserRoleProvisioner.class);

        PlatformInviteEntity invite = pendingInvite(PlatformInviteDeliveryMode.ONLINE, null);
        invite.setEmail("bob@example.com");
        when(inviteOperator.requirePendingByToken("tok")).thenReturn(invite);
        when(passwordDecryptor.decrypt("enc")).thenReturn("raw-pass");
        when(userService.createActiveUserWithPassword(any(), any(), any(), any()))
                .thenReturn(new PlatformUserVo(
                        "usr-2",
                        "bob",
                        null,
                        "bob@example.com",
                        null,
                        null,
                        PlatformUserStatus.ACTIVE.name(),
                        null,
                        null,
                        null));

        PlatformPublicInviteService service = new PlatformPublicInviteService(
                inviteOperator,
                userService,
                otpChallengeService,
                passwordDecryptor,
                modeService,
                roleProvisioner);

        service.acceptInvite("tok", new PlatformInviteAcceptRequest(null, "enc", "123456", null));

        verify(otpChallengeService).verifyOrThrow(ArgumentMatchers.argThat(cmd ->
                cmd.purpose() == OtpPurpose.PLATFORM_INVITE_ACCEPT
                        && cmd.channel() == OtpChannel.EMAIL
                        && "bob@example.com".equals(cmd.destination())
                        && "123456".equals(cmd.code())));
    }

    @Test
    void activateByInviteCodeRequiresInviteRegistrationMode() {
        PlatformInviteOperator inviteOperator = mock(PlatformInviteOperator.class);
        PlatformUserService userService = mock(PlatformUserService.class);
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        PasswordDecryptor passwordDecryptor = mock(PasswordDecryptor.class);
        PlatformRegistrationModeSettingService modeService = mock(PlatformRegistrationModeSettingService.class);
        PlatformUserRoleProvisioner roleProvisioner = mock(PlatformUserRoleProvisioner.class);

        org.mockito.Mockito.doThrow(NexusException.build(
                        PlatformRegistrationModeSettingStatusCode.REGISTRATION_MODE_DISABLED))
                .when(modeService)
                .requireSelfServiceMode(PlatformRegistrationMode.INVITE);

        PlatformPublicInviteService service = new PlatformPublicInviteService(
                inviteOperator,
                userService,
                otpChallengeService,
                passwordDecryptor,
                modeService,
                roleProvisioner);

        assertThatThrownBy(() -> service.activateByInviteCode(new PlatformInviteActivateByCodeRequest(
                        "CODE1",
                        "alice@example.com",
                        null,
                        "enc")))
                .isInstanceOf(NexusException.class);
        verify(inviteOperator, never()).requirePendingByCode(any());
    }

    @Test
    void activateByInviteCodeRejectsContactMismatch() {
        PlatformInviteOperator inviteOperator = mock(PlatformInviteOperator.class);
        PlatformUserService userService = mock(PlatformUserService.class);
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        PasswordDecryptor passwordDecryptor = mock(PasswordDecryptor.class);
        PlatformRegistrationModeSettingService modeService = mock(PlatformRegistrationModeSettingService.class);
        PlatformUserRoleProvisioner roleProvisioner = mock(PlatformUserRoleProvisioner.class);

        PlatformInviteEntity invite = pendingInvite(PlatformInviteDeliveryMode.OFFLINE, null);
        invite.setEmail("alice@example.com");
        when(inviteOperator.requirePendingByCode("CODE1")).thenReturn(invite);
        when(inviteOperator.registerCodeFailure(invite)).thenReturn(false);

        PlatformPublicInviteService service = new PlatformPublicInviteService(
                inviteOperator,
                userService,
                otpChallengeService,
                passwordDecryptor,
                modeService,
                roleProvisioner);

        assertThatThrownBy(() -> service.activateByInviteCode(new PlatformInviteActivateByCodeRequest(
                        "CODE1",
                        "wrong@example.com",
                        null,
                        "enc")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformInviteStatusCode.INVITE_CONTACT_MISMATCH.fullCode());
    }

    private static PlatformInviteEntity pendingInvite(PlatformInviteDeliveryMode mode, String roleCodes) {
        PlatformInviteEntity entity = new PlatformInviteEntity();
        entity.setInviteId("piv-test");
        entity.setInviteToken("tok");
        entity.setInviteCode("CODE1");
        entity.setEmail("alice@example.com");
        entity.setLoginName("alice");
        entity.setDefaultRoleCodes(roleCodes);
        entity.setStatus(PlatformInviteStatus.PENDING.name());
        entity.setDeliveryMode(mode.name());
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        entity.setCodeFailedAttempts(0);
        return entity;
    }
}

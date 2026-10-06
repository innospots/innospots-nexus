package com.innospots.nexus.platform.access.service;

import org.junit.jupiter.api.Test;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class PlatformOpenRegistrationServiceTest {

    @Test
    void issueVerificationOtpMapsOtpResendTooFrequentToAccessStatus() {
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        doThrow(NexusException.build(OtpStatusCode.RESEND_TOO_FREQUENT))
                .when(otpChallengeService)
                .issue(any());

        PlatformOpenRegistrationService service = new PlatformOpenRegistrationService(
                mock(com.innospots.nexus.platform.user.service.PlatformUserService.class),
                otpChallengeService,
                mock(PasswordDecryptor.class),
                mock(PlatformRegistrationModeSettingService.class));

        assertThatThrownBy(() -> service.issueVerificationOtp(
                        new PlatformRegistrationOtpRequest(null, null, "user@example.com", null)))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformAccessStatusCode.ACCESS_OTP_RESEND_TOO_FREQUENT.fullCode());
    }
}

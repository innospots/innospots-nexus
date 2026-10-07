package com.innospots.nexus.platform.access.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.console.credential.password.PasswordDecryptor;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.operator.PlatformAccessRequestOperator;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;
import com.innospots.nexus.platform.settings.service.PlatformRegistrationModeSettingService;
import com.innospots.nexus.platform.user.service.PlatformUserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlatformAccessRequestServiceTest {

    @Test
    void issueAccessVerificationOtpMapsOtpResendTooFrequentToAccessStatus() {
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        doThrow(NexusException.build(OtpStatusCode.RESEND_TOO_FREQUENT))
                .when(otpChallengeService)
                .issue(any());

        PlatformAccessRequestService service = newService(otpChallengeService);

        assertThatThrownBy(() -> service.issueAccessVerificationOtp(
                        new PlatformRegistrationOtpRequest(null, null, "user@example.com", "zh")))
                .isInstanceOf(NexusException.class)
                .extracting(ex -> ((NexusException) ex).code())
                .isEqualTo(PlatformAccessStatusCode.ACCESS_OTP_RESEND_TOO_FREQUENT.fullCode());
    }

    @Test
    void issueAccessVerificationOtpUsesEmailFromFormWhenBothEmailAndMobilePresent() {
        OtpChallengeService otpChallengeService = mock(OtpChallengeService.class);
        PlatformAccessRequestService service = newService(otpChallengeService);

        service.issueAccessVerificationOtp(
                new PlatformRegistrationOtpRequest("a@b.com", "13800000001", null, null));

        ArgumentCaptor<OtpIssueCommand> captor = ArgumentCaptor.forClass(OtpIssueCommand.class);
        verify(otpChallengeService).issue(captor.capture());
        assertThat(captor.getValue().purpose()).isEqualTo(OtpPurpose.PLATFORM_ACCESS_VERIFY);
        assertThat(captor.getValue().destination()).isEqualTo("a@b.com");
    }

    private static PlatformAccessRequestService newService(OtpChallengeService otpChallengeService) {
        return new PlatformAccessRequestService(
                mock(PlatformAccessRequestOperator.class),
                mock(PlatformUserService.class),
                otpChallengeService,
                mock(PasswordDecryptor.class),
                mock(PlatformRegistrationModeSettingService.class));
    }
}

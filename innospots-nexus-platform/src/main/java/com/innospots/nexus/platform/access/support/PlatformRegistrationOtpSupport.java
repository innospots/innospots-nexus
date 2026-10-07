package com.innospots.nexus.platform.access.support;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.console.credential.otp.status.OtpStatusCode;
import com.innospots.nexus.platform.access.status.PlatformAccessStatusCode;

/**
 * 平台注册 OTP 下发（含 60s 重发冷却与 {@link PlatformAccessStatusCode#ACCESS_OTP_RESEND_TOO_FREQUENT} 映射）。
 */
public final class PlatformRegistrationOtpSupport {

    private PlatformRegistrationOtpSupport() {
    }

    public static void issue(
            OtpChallengeService otpChallengeService,
            OtpPurpose purpose,
            String email,
            String mobile,
            String legacyContact,
            String locale
    ) {
        String contact = PlatformRegistrationContactSupport.resolveVerificationContactForOtpRequest(
                email,
                mobile,
                legacyContact);
        OtpChannel channel = PlatformRegistrationContactSupport.resolveChannel(contact);
        OtpIssueCommand command = new OtpIssueCommand(
                SecurityRealm.PLATFORM,
                purpose,
                channel,
                contact,
                locale);
        try {
            otpChallengeService.issue(command);
        } catch (NexusException ex) {
            if (OtpStatusCode.RESEND_TOO_FREQUENT.fullCode().equals(ex.code())) {
                throw NexusException.build(PlatformAccessStatusCode.ACCESS_OTP_RESEND_TOO_FREQUENT);
            }
            throw ex;
        }
    }
}

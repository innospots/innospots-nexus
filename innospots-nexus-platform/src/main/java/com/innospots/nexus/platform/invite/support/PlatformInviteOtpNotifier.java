package com.innospots.nexus.platform.invite.support;

import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.credential.otp.domain.OtpIssueCommand;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpChannel;
import com.innospots.nexus.console.credential.otp.domain.enums.OtpPurpose;
import com.innospots.nexus.console.credential.otp.service.OtpChallengeService;
import com.innospots.nexus.platform.invite.domain.entity.PlatformInviteEntity;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteDeliveryMode;
import com.innospots.nexus.platform.invite.status.PlatformInviteStatusCode;

import com.innospots.nexus.base.exception.NexusException;

/**
 * 在线邀请交付：经 {@link OtpChallengeService} 发布 {@link com.innospots.nexus.console.credential.otp.event.OtpSendRequestedEvent}。
 */
public final class PlatformInviteOtpNotifier {

    private final OtpChallengeService otpChallengeService;

    public PlatformInviteOtpNotifier(OtpChallengeService otpChallengeService) {
        this.otpChallengeService = otpChallengeService;
    }

    public void deliverInvite(PlatformInviteEntity invite, OtpPurpose purpose, String locale) {
        if (invite.deliveryModeEnum() != PlatformInviteDeliveryMode.ONLINE) {
            return;
        }
        OtpChannel channel = resolveChannel(invite);
        String destination = channel == OtpChannel.EMAIL ? invite.getEmail() : invite.getMobile();
        otpChallengeService.issue(new OtpIssueCommand(
                SecurityRealm.PLATFORM,
                purpose,
                channel,
                destination,
                locale));
    }

    private static OtpChannel resolveChannel(PlatformInviteEntity invite) {
        if (invite.getEmail() != null && !invite.getEmail().isBlank()) {
            return OtpChannel.EMAIL;
        }
        if (invite.getMobile() != null && !invite.getMobile().isBlank()) {
            return OtpChannel.MOBILE;
        }
        throw NexusException.build(PlatformInviteStatusCode.INVITE_CONTACT_REQUIRED);
    }
}

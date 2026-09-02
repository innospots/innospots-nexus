package com.innospots.nexus.kernel.user.domain.vo;

import com.innospots.nexus.kernel.user.domain.enums.UserRegisterSource;
import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * Read model for tenant-realm user profile data.
 *
 * @param userId         tenant-realm user identifier
 * @param userName       unique login user name
 * @param displayName    display name shown in UI
 * @param email          email address
 * @param mobile         mobile phone number
 * @param region         region preference
 * @param timeZone       IANA time zone
 * @param language       UI language
 * @param avatarKey      avatar storage key
 * @param registerSource original registration source
 * @param status         user lifecycle status
 */
public record UserProfileVo(
        String userId,
        String userName,
        String displayName,
        String email,
        String mobile,
        String region,
        String timeZone,
        String language,
        String avatarKey,
        UserRegisterSource registerSource,
        UserStatus status
) {
}

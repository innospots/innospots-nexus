package com.innospots.nexus.kernel.user.domain.vo;

import com.innospots.nexus.kernel.user.enums.UserStatus;
import com.innospots.nexus.kernel.user.enums.UserRegisterSource;

/**
 * Read model for user profile data returned by user data operations.
 *
 * @param userId         user identifier
 * @param userName       unique login user name
 * @param displayName    display name shown in UI
 * @param realName       legal or real-world name when provided
 * @param email          email address
 * @param mobile         mobile phone number
 * @param avatarKey      avatar storage key
 * @param registerSource original registration source
 * @param status         user lifecycle status
 */
public record UserProfileVO(
        String userId,
        String userName,
        String displayName,
        String realName,
        String email,
        String mobile,
        String avatarKey,
        UserRegisterSource registerSource,
        UserStatus status
) {
}

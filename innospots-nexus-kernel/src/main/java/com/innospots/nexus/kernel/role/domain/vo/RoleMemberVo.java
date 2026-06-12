package com.innospots.nexus.kernel.role.domain.vo;

import java.time.LocalDateTime;

import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * User summary displayed in role membership management.
 *
 * @param userId       user identifier
 * @param userName     login name
 * @param displayName  display name
 * @param email        email address
 * @param mobile       mobile number
 * @param avatarKey    avatar resource key
 * @param status       user lifecycle status
 * @param assignedTime role assignment time
 */
public record RoleMemberVo(
        String userId,
        String userName,
        String displayName,
        String email,
        String mobile,
        String avatarKey,
        UserStatus status,
        LocalDateTime assignedTime
) {
}

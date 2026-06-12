package com.innospots.nexus.kernel.group.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.innospots.nexus.kernel.user.domain.enums.UserStatus;

/**
 * User summary displayed in group membership management.
 *
 * @param userId       user identifier
 * @param userName     login name
 * @param displayName  display name
 * @param email        email address
 * @param mobile       mobile number
 * @param avatarKey    avatar resource key
 * @param status       user lifecycle status
 * @param leader       whether the user leads the group
 * @param assignedTime group assignment time
 * @param tags         member capability tags
 */
public record GroupMemberVo(
        String userId,
        String userName,
        String displayName,
        String email,
        String mobile,
        String avatarKey,
        UserStatus status,
        Boolean leader,
        LocalDateTime assignedTime,
        List<GroupMemberTagVo> tags
) {

    public GroupMemberVo {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}

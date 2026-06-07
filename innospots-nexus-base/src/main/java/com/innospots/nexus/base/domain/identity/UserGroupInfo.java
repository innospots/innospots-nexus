package com.innospots.nexus.base.domain.identity;

import com.innospots.nexus.base.domain.enums.BasicStatus;

import java.util.List;

/**
 * A user group/team with a hierarchy (parent group), a head user,
 * and assistant users.
 */
public record UserGroupInfo(
        Long groupId,
        String groupName,
        String groupCode,
        Long parentGroupId,
        Long headUserId,
        List<Long> assistantUserIds,
        BasicStatus status
) {

    public UserGroupInfo {
        assistantUserIds = assistantUserIds == null ? List.of() : List.copyOf(assistantUserIds);
    }
}

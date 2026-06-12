package com.innospots.nexus.kernel.group.domain.request;

import java.util.List;

/**
 * Users to assign or move to one group.
 *
 * @param userIds user identifiers
 */
public record GroupMemberAssignRequest(List<String> userIds) {

    public GroupMemberAssignRequest {
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
    }
}

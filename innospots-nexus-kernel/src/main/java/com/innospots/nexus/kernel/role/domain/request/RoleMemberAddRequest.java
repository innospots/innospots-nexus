package com.innospots.nexus.kernel.role.domain.request;

import java.util.List;

/**
 * Request for adding users to a role without removing existing members.
 *
 * @param userIds user identifiers to add
 */
public record RoleMemberAddRequest(List<String> userIds) {

    public RoleMemberAddRequest {
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
    }
}

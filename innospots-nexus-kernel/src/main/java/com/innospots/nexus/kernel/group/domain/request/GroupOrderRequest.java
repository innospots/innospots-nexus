package com.innospots.nexus.kernel.group.domain.request;

import java.util.List;

/**
 * Ordered sibling group identifiers.
 *
 * @param parentId optional parent identifier for root groups
 * @param groupIds group identifiers in target order
 */
public record GroupOrderRequest(String parentId, List<String> groupIds) {

    public GroupOrderRequest {
        groupIds = groupIds == null ? List.of() : List.copyOf(groupIds);
    }
}

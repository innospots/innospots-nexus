package com.innospots.nexus.kernel.group.domain.request;

import java.util.List;

/**
 * Project tag names to assign to a group member.
 *
 * @param tagNames tag names
 */
public record GroupMemberTagAddRequest(List<String> tagNames) {

    public GroupMemberTagAddRequest {
        tagNames = tagNames == null ? List.of() : List.copyOf(tagNames);
    }
}

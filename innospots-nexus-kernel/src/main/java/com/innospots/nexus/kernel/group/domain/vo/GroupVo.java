package com.innospots.nexus.kernel.group.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Management group details and nested child groups.
 *
 * @param groupId     group identifier
 * @param parentId    optional parent identifier
 * @param groupCode   stable group code
 * @param groupName   display name
 * @param description optional description
 * @param status      lifecycle status
 * @param sortOrder   sibling display order
 * @param builtIn     whether the group is protected
 * @param memberCount direct member count
 * @param leaderCount direct leader count
 * @param createdTime creation time
 * @param updatedTime last update time
 * @param children    child groups
 */
public record GroupVo(
        String groupId,
        String parentId,
        String groupCode,
        String groupName,
        String description,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        long memberCount,
        long leaderCount,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<GroupVo> children
) {

    public GroupVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

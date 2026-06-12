package com.innospots.nexus.kernel.group.domain.vo;

/**
 * Compact group option for parent and assignment controls.
 *
 * @param groupId   group identifier
 * @param parentId  optional parent identifier
 * @param groupName display name
 * @param disabled  whether the option cannot be selected
 */
public record GroupOptionVo(
        String groupId,
        String parentId,
        String groupName,
        Boolean disabled
) {
}

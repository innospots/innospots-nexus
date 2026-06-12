package com.innospots.nexus.kernel.group.domain.request;

/**
 * Data required to create a hierarchical group.
 *
 * @param parentId    optional parent group identifier
 * @param groupCode   stable project-unique group code
 * @param groupName   display name
 * @param description optional description
 * @param sortOrder   sibling display order
 */
public record GroupCreateRequest(
        String parentId,
        String groupCode,
        String groupName,
        String description,
        Integer sortOrder
) {
}

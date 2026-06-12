package com.innospots.nexus.kernel.group.domain.request;

/**
 * Mutable fields of an existing group.
 *
 * @param parentId    optional parent group identifier
 * @param groupName   display name
 * @param description optional description
 * @param sortOrder   sibling display order
 */
public record GroupUpdateRequest(
        String parentId,
        String groupName,
        String description,
        Integer sortOrder
) {
}

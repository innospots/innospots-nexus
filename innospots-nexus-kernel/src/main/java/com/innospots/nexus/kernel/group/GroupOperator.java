package com.innospots.nexus.kernel.group;

import java.util.List;
import java.util.Optional;

/**
 * Port for department and user-group operations.
 */
public interface GroupOperator {

    /**
     * Finds a group by identifier.
     *
     * @param groupId group identifier
     * @return group when found
     */
    Optional<UserGroup> findGroup(String groupId);

    /**
     * Lists child groups.
     *
     * @param parentId parent group identifier
     * @return child groups
     */
    List<UserGroup> listChildren(String parentId);

    /**
     * Saves a group.
     *
     * @param group group to save
     * @return saved group
     */
    UserGroup saveGroup(UserGroup group);
}

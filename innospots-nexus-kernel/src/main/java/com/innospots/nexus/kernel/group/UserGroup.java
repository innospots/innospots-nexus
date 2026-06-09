package com.innospots.nexus.kernel.group;

/**
 * User group or department entity.
 *
 * @param groupId   group identifier
 * @param parentId  parent group identifier
 * @param name      group name
 * @param type      group type
 * @param enabled   whether the group is active
 */
public record UserGroup(String groupId, String parentId, String name, GroupType type, boolean enabled) {
}

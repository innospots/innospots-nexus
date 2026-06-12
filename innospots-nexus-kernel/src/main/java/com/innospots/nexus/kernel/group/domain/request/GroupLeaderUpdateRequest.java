package com.innospots.nexus.kernel.group.domain.request;

/**
 * Leader flag update for an existing group member.
 *
 * @param leader whether the member is a group leader
 */
public record GroupLeaderUpdateRequest(Boolean leader) {
}

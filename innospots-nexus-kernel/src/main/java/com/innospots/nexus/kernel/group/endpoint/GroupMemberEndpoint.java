package com.innospots.nexus.kernel.group.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.group.domain.request.GroupLeaderUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberAssignRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberPageRequest;
import com.innospots.nexus.kernel.group.domain.vo.GroupMemberVo;

/**
 * Management endpoint for single-ownership group membership.
 */
@Path("/console/groups/{groupId}/members")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupMemberEndpoint {

    /**
     * Pages direct members of a group.
     *
     * @param groupId group identifier
     * @param request member filters
     * @return matching member page
     */
    @GET
    public R<PageResult<GroupMemberVo>> pageGroupMembers(
            @PathParam("groupId") String groupId,
            @BeanParam GroupMemberPageRequest request
    ) {
        // TODO Query membership, users, and tags separately and assemble without joins.
        throw new UnsupportedOperationException("Group member paging is not implemented");
    }

    /**
     * Assigns or moves users to a group.
     *
     * @param groupId group identifier
     * @param request users to assign
     * @return empty success response
     */
    @PUT
    public R<Void> assignGroupMembers(
            @PathParam("groupId") String groupId,
            GroupMemberAssignRequest request
    ) {
        // TODO Implement transactional single-group ownership and stale tag cleanup.
        throw new UnsupportedOperationException("Group member assignment is not implemented");
    }

    /**
     * Removes a user from the group.
     *
     * @param groupId group identifier
     * @param userId  user identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{userId}")
    public R<Void> removeGroupMember(
            @PathParam("groupId") String groupId,
            @PathParam("userId") String userId
    ) {
        // TODO Remove membership and member tag assignments transactionally.
        throw new UnsupportedOperationException("Group member removal is not implemented");
    }

    /**
     * Sets or clears a member's leader flag.
     *
     * @param groupId group identifier
     * @param userId  member user identifier
     * @param request target leader flag
     * @return empty success response
     */
    @PUT
    @Path("/{userId}/leader")
    public R<Void> updateGroupLeader(
            @PathParam("groupId") String groupId,
            @PathParam("userId") String userId,
            GroupLeaderUpdateRequest request
    ) {
        // TODO Validate current membership and update the leader flag.
        throw new UnsupportedOperationException("Group leader update is not implemented");
    }
}

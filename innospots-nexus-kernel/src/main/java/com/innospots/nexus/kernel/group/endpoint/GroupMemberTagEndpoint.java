package com.innospots.nexus.kernel.group.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.group.domain.request.GroupMemberTagAddRequest;
import com.innospots.nexus.kernel.group.domain.vo.GroupMemberTagVo;

/**
 * Management endpoint for project tag assignments within one group membership.
 */
@Path("/console/groups/{groupId}/members/{userId}/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupMemberTagEndpoint {

    /**
     * Lists tags assigned to a group member.
     *
     * @param groupId group identifier
     * @param userId  member user identifier
     * @return assigned tags
     */
    @GET
    public R<List<GroupMemberTagVo>> listMemberTags(
            @PathParam("groupId") String groupId,
            @PathParam("userId") String userId
    ) {
        // TODO Query assignments and tag definitions separately and assemble by tag name.
        throw new UnsupportedOperationException("Group member tag query is not implemented");
    }

    /**
     * Adds project tags to a group member.
     *
     * @param groupId group identifier
     * @param userId  member user identifier
     * @param request tag names to add
     * @return empty success response
     */
    @POST
    public R<Void> addMemberTags(
            @PathParam("groupId") String groupId,
            @PathParam("userId") String userId,
            GroupMemberTagAddRequest request
    ) {
        // TODO Validate membership and tag names through separate single-table queries.
        throw new UnsupportedOperationException("Group member tag assignment is not implemented");
    }

    /**
     * Removes one tag from a group member.
     *
     * @param groupId group identifier
     * @param userId  member user identifier
     * @param tagName assigned tag name
     * @return empty success response
     */
    @DELETE
    @Path("/{tagName}")
    public R<Void> removeMemberTag(
            @PathParam("groupId") String groupId,
            @PathParam("userId") String userId,
            @PathParam("tagName") String tagName
    ) {
        // TODO Remove the exact single-table tag assignment.
        throw new UnsupportedOperationException("Group member tag removal is not implemented");
    }
}

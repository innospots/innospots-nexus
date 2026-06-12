package com.innospots.nexus.kernel.group.endpoint;

import java.util.List;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.group.domain.request.GroupCreateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupOrderRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupStatusUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupTreeRequest;
import com.innospots.nexus.kernel.group.domain.request.GroupUpdateRequest;
import com.innospots.nexus.kernel.group.domain.vo.GroupOptionVo;
import com.innospots.nexus.kernel.group.domain.vo.GroupVo;

/**
 * Management endpoint for group hierarchy and lifecycle operations.
 */
@Path("/console/groups")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GroupEndpoint {

    /**
     * Lists the group hierarchy using optional filters.
     *
     * @param request tree filters
     * @return matching group tree
     */
    @GET
    public R<List<GroupVo>> listGroupTree(@BeanParam GroupTreeRequest request) {
        // TODO Implement single-table group queries and in-memory tree assembly.
        throw new UnsupportedOperationException("Group tree query is not implemented");
    }

    /**
     * Returns one group.
     *
     * @param groupId group identifier
     * @return group details
     */
    @GET
    @Path("/{groupId}")
    public R<GroupVo> getGroup(@PathParam("groupId") String groupId) {
        // TODO Implement group lookup and separately assembled counts.
        throw new UnsupportedOperationException("Group lookup is not implemented");
    }

    /**
     * Creates a group.
     *
     * @param request group creation data
     * @return created group
     */
    @POST
    public R<GroupVo> createGroup(GroupCreateRequest request) {
        // TODO Implement group creation through the group service.
        throw new UnsupportedOperationException("Group creation is not implemented");
    }

    /**
     * Updates mutable group fields.
     *
     * @param groupId group identifier
     * @param request group update data
     * @return updated group
     */
    @PUT
    @Path("/{groupId}")
    public R<GroupVo> updateGroup(
            @PathParam("groupId") String groupId,
            GroupUpdateRequest request
    ) {
        // TODO Implement hierarchy validation and group update.
        throw new UnsupportedOperationException("Group update is not implemented");
    }

    /**
     * Enables or disables a group.
     *
     * @param groupId group identifier
     * @param request target status
     * @return empty success response
     */
    @PUT
    @Path("/{groupId}/status")
    public R<Void> updateGroupStatus(
            @PathParam("groupId") String groupId,
            GroupStatusUpdateRequest request
    ) {
        // TODO Implement protected and hierarchical status rules.
        throw new UnsupportedOperationException("Group status update is not implemented");
    }

    /**
     * Reorders sibling groups.
     *
     * @param request ordered sibling identifiers
     * @return empty success response
     */
    @PUT
    @Path("/order")
    public R<Void> reorderGroups(GroupOrderRequest request) {
        // TODO Implement sibling ordering through the group service.
        throw new UnsupportedOperationException("Group ordering is not implemented");
    }

    /**
     * Deletes a removable group.
     *
     * @param groupId group identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{groupId}")
    public R<Void> deleteGroup(@PathParam("groupId") String groupId) {
        // TODO Implement child, member, tag assignment, and protected-record checks.
        throw new UnsupportedOperationException("Group deletion is not implemented");
    }

    /**
     * Lists compact groups for selectors.
     *
     * @return group options
     */
    @GET
    @Path("/options")
    public R<List<GroupOptionVo>> listGroupOptions() {
        // TODO Implement single-table option queries and in-memory hierarchy assembly.
        throw new UnsupportedOperationException("Group option query is not implemented");
    }
}

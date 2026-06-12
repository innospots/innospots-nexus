package com.innospots.nexus.kernel.group.endpoint;

import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.kernel.group.domain.request.TagCreateRequest;
import com.innospots.nexus.kernel.group.domain.request.TagStatusUpdateRequest;
import com.innospots.nexus.kernel.group.domain.request.TagUpdateRequest;
import com.innospots.nexus.kernel.group.domain.vo.TagVo;

/**
 * Management endpoint for lightweight project-level tag definitions.
 */
@Path("/console/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TagEndpoint {

    /**
     * Lists project tags.
     *
     * @param status optional lifecycle status
     * @return project tags
     */
    @GET
    public R<List<TagVo>> listTags(@QueryParam("status") BasicStatus status) {
        // TODO Query tag definitions and assignment counts in separate batches.
        throw new UnsupportedOperationException("Tag query is not implemented");
    }

    /**
     * Creates a project tag.
     *
     * @param request tag creation data
     * @return created tag
     */
    @POST
    public R<TagVo> createTag(TagCreateRequest request) {
        // TODO Implement project-unique tag creation.
        throw new UnsupportedOperationException("Tag creation is not implemented");
    }

    /**
     * Updates a project tag.
     *
     * @param tagId   tag identifier
     * @param request target tag data
     * @return updated tag
     */
    @PUT
    @Path("/{tagId}")
    public R<TagVo> updateTag(
            @PathParam("tagId") String tagId,
            TagUpdateRequest request
    ) {
        // TODO Rename tag definitions and assignments through a transactional service.
        throw new UnsupportedOperationException("Tag update is not implemented");
    }

    /**
     * Enables or disables a project tag.
     *
     * @param tagId   tag identifier
     * @param request target status
     * @return empty success response
     */
    @PUT
    @Path("/{tagId}/status")
    public R<Void> updateTagStatus(
            @PathParam("tagId") String tagId,
            TagStatusUpdateRequest request
    ) {
        // TODO Implement tag status updates through the tag service.
        throw new UnsupportedOperationException("Tag status update is not implemented");
    }

    /**
     * Deletes an unused project tag.
     *
     * @param tagId tag identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{tagId}")
    public R<Void> deleteTag(@PathParam("tagId") String tagId) {
        // TODO Check assignments separately before deleting the tag definition.
        throw new UnsupportedOperationException("Tag deletion is not implemented");
    }
}

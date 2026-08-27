package com.innospots.nexus.console.dictionary.endpoint;

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

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;

/**
 * Management-console endpoint for dictionary items nested under a type code.
 * <p>
 * Method workflows are deferred until the dictionary service and operator
 * boundaries are implemented.
 * </p>
 */
@Path("/console/dictionary-types/{typeCode}/items")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DictionaryItemEndpoint {

    /**
     * Pages dictionary items for one type.
     *
     * @param typeCode parent type code
     * @param request  item page query
     * @return matching item page
     */
    @GET
    public R<PageResult<DictionaryItemVo>> pageDictionaryItems(
            @PathParam("typeCode") String typeCode,
            @BeanParam DictionaryItemPageRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary item paging is not implemented");
    }

    /**
     * Creates a dictionary item under the type code.
     *
     * @param typeCode parent type code
     * @param request  item creation data
     * @return created item
     */
    @POST
    public R<DictionaryItemVo> createDictionaryItem(
            @PathParam("typeCode") String typeCode,
            DictionaryItemCreateRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary item creation is not implemented");
    }

    /**
     * Updates mutable dictionary item fields without changing its stable value.
     *
     * @param typeCode           parent type code
     * @param dictionaryItemId   item identifier
     * @param request            item update data
     * @return updated item
     */
    @PUT
    @Path("/{dictionaryItemId}")
    public R<DictionaryItemVo> updateDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary item update is not implemented");
    }

    /**
     * Enables or disables a dictionary item.
     *
     * @param typeCode         parent type code
     * @param dictionaryItemId item identifier
     * @param request          target status
     * @return empty success response
     */
    @PUT
    @Path("/{dictionaryItemId}/status")
    public R<Void> updateDictionaryItemStatus(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary item status update is not implemented");
    }

    /**
     * Deletes a removable dictionary item.
     *
     * @param typeCode         parent type code
     * @param dictionaryItemId item identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{dictionaryItemId}")
    public R<Void> deleteDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId
    ) {
        throw new UnsupportedOperationException("Dictionary item deletion is not implemented");
    }
}

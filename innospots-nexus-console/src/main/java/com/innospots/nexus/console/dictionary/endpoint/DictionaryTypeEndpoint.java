package com.innospots.nexus.console.dictionary.endpoint;

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

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;

/**
 * Management-console endpoint for dictionary type catalogs.
 * <p>
 * Method workflows are deferred until the dictionary service and operator
 * boundaries are implemented.
 * </p>
 */
@Path("/console/dictionary-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DictionaryTypeEndpoint {

    /**
     * Pages dictionary types using management filters.
     *
     * @param request type page query
     * @return matching type page
     */
    @GET
    public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(@BeanParam DictionaryTypePageRequest request) {
        throw new UnsupportedOperationException("Dictionary type paging is not implemented");
    }

    /**
     * Returns one dictionary type by identifier.
     *
     * @param dictionaryTypeId type identifier
     * @return type details
     */
    @GET
    @Path("/{dictionaryTypeId}")
    public R<DictionaryTypeVo> getDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        throw new UnsupportedOperationException("Dictionary type lookup is not implemented");
    }

    /**
     * Creates a dictionary type.
     *
     * @param request type creation data
     * @return created type
     */
    @POST
    public R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request) {
        throw new UnsupportedOperationException("Dictionary type creation is not implemented");
    }

    /**
     * Updates mutable dictionary type fields without changing its stable code.
     *
     * @param dictionaryTypeId type identifier
     * @param request          type update data
     * @return updated type
     */
    @PUT
    @Path("/{dictionaryTypeId}")
    public R<DictionaryTypeVo> updateDictionaryType(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary type update is not implemented");
    }

    /**
     * Enables or disables a dictionary type.
     *
     * @param dictionaryTypeId type identifier
     * @param request          target status
     * @return empty success response
     */
    @PUT
    @Path("/{dictionaryTypeId}/status")
    public R<Void> updateDictionaryTypeStatus(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeStatusUpdateRequest request
    ) {
        throw new UnsupportedOperationException("Dictionary type status update is not implemented");
    }

    /**
     * Deletes a removable dictionary type.
     *
     * @param dictionaryTypeId type identifier
     * @return empty success response
     */
    @DELETE
    @Path("/{dictionaryTypeId}")
    public R<Void> deleteDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        throw new UnsupportedOperationException("Dictionary type deletion is not implemented");
    }

    /**
     * Lists compact dictionary type options for selectors.
     *
     * @return type options
     */
    @GET
    @Path("/options")
    public R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions() {
        throw new UnsupportedOperationException("Dictionary type option query is not implemented");
    }
}

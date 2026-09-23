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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.service.DictionaryService;

/**
 * 字典类型管理 REST 资源。
 */
@Path("/console/dictionary-types")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
public class DictionaryTypeEndpoint {

    private final DictionaryService dictionaryService;

    @GET
    public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(@BeanParam DictionaryTypePageRequest request) {
        return R.ok(dictionaryService.pageDictionaryTypes(request));
    }

    @GET
    @Path("/{dictionaryTypeId}")
    public R<DictionaryTypeVo> getDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        return R.ok(dictionaryService.getDictionaryType(dictionaryTypeId));
    }

    @POST
    public R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request) {
        return R.ok(dictionaryService.createDictionaryType(request));
    }

    @PUT
    @Path("/{dictionaryTypeId}")
    public R<DictionaryTypeVo> updateDictionaryType(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeUpdateRequest request
    ) {
        return R.ok(dictionaryService.updateDictionaryType(dictionaryTypeId, request));
    }

    @PUT
    @Path("/{dictionaryTypeId}/status")
    public R<Void> updateDictionaryTypeStatus(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeStatusUpdateRequest request
    ) {
        dictionaryService.updateDictionaryTypeStatus(dictionaryTypeId, request);
        return R.ok();
    }

    @DELETE
    @Path("/{dictionaryTypeId}")
    public R<Void> deleteDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        dictionaryService.deleteDictionaryType(dictionaryTypeId);
        return R.ok();
    }

    @GET
    @Path("/options")
    public R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(@QueryParam("status") BasicStatus status) {
        return R.ok(dictionaryService.listDictionaryTypeOptions(status));
    }
}

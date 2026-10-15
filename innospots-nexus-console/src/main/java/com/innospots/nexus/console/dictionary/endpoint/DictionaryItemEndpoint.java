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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 字典项管理 REST 契约。
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Dictionary", description = "字典类型与字典项")
@NexusAuthenticatedApi
public interface DictionaryItemEndpoint {

    @GET
    @Operation(operationId = "dictionaryItemPage", summary = "分页查询字典项")
    R<PageResult<DictionaryItemVo>> pageDictionaryItems(
            @PathParam("typeCode") String typeCode,
            @BeanParam DictionaryItemPageRequest request
    );

    @POST
    @Operation(operationId = "dictionaryItemCreate", summary = "创建字典项")
    R<DictionaryItemVo> createDictionaryItem(
            @PathParam("typeCode") String typeCode,
            DictionaryItemCreateRequest request
    );

    @PUT
    @Path("/{dictionaryItemId}")
    @Operation(operationId = "dictionaryItemUpdate", summary = "更新字典项")
    R<DictionaryItemVo> updateDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemUpdateRequest request
    );

    @PUT
    @Path("/{dictionaryItemId}/status")
    @Operation(operationId = "dictionaryItemUpdateStatus", summary = "更新字典项状态")
    R<Void> updateDictionaryItemStatus(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request
    );

    @DELETE
    @Path("/{dictionaryItemId}")
    @Operation(operationId = "dictionaryItemDelete", summary = "删除字典项")
    R<Void> deleteDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId
    );
}

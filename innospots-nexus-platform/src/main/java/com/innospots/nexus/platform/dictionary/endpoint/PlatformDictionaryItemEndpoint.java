package com.innospots.nexus.platform.dictionary.endpoint;

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
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryItemEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.dictionary.service.PlatformDictionaryService;

@Path(PlatformConstant.DICTIONARY_ITEMS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformDictionary", description = "运营平台字典项")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformDictionaryItemEndpoint implements DictionaryItemEndpoint {

    private final PlatformDictionaryService platformDictionaryService;

    @Override
    @GET
    @Operation(operationId = "platformDictionaryItemPage", summary = "分页查询平台字典项")
    public R<PageResult<DictionaryItemVo>> pageDictionaryItems(
            @PathParam("typeCode") String typeCode,
            @BeanParam DictionaryItemPageRequest request) {
        return R.ok(platformDictionaryService.pageDictionaryItems(typeCode, request));
    }

    @Override
    @POST
    @Operation(operationId = "platformDictionaryItemCreate", summary = "创建平台字典项")
    public R<DictionaryItemVo> createDictionaryItem(
            @PathParam("typeCode") String typeCode,
            DictionaryItemCreateRequest request) {
        return R.ok(platformDictionaryService.createDictionaryItem(typeCode, request));
    }

    @Override
    @PUT
    @Path("/{dictionaryItemId}")
    @Operation(operationId = "platformDictionaryItemUpdate", summary = "更新平台字典项")
    public R<DictionaryItemVo> updateDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemUpdateRequest request) {
        return R.ok(platformDictionaryService.updateDictionaryItem(typeCode, dictionaryItemId, request));
    }

    @Override
    @PUT
    @Path("/{dictionaryItemId}/status")
    @Operation(operationId = "platformDictionaryItemUpdateStatus", summary = "更新平台字典项状态")
    public R<Void> updateDictionaryItemStatus(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId,
            DictionaryItemStatusUpdateRequest request) {
        platformDictionaryService.updateDictionaryItemStatus(typeCode, dictionaryItemId, request);
        return R.ok();
    }

    @Override
    @DELETE
    @Path("/{dictionaryItemId}")
    @Operation(operationId = "platformDictionaryItemDelete", summary = "删除平台字典项")
    public R<Void> deleteDictionaryItem(
            @PathParam("typeCode") String typeCode,
            @PathParam("dictionaryItemId") String dictionaryItemId) {
        platformDictionaryService.deleteDictionaryItem(typeCode, dictionaryItemId);
        return R.ok();
    }
}

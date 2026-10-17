package com.innospots.nexus.platform.dictionary.endpoint;

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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryItemPageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryItemVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.console.dictionary.endpoint.DictionaryTypeEndpoint;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.dictionary.service.PlatformDictionaryService;

@Path(PlatformConstant.DICTIONARY_TYPES_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformDictionary", description = "运营平台字典类型")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformDictionaryTypeEndpoint implements DictionaryTypeEndpoint {

    private final PlatformDictionaryService platformDictionaryService;

    @Override
    @GET
    @Operation(operationId = "platformDictionaryTypePage", summary = "分页查询平台字典类型")
    public R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(@BeanParam DictionaryTypePageRequest request) {
        return R.ok(platformDictionaryService.pageDictionaryTypes(request));
    }

    @Override
    @GET
    @Path("/{dictionaryTypeId}")
    @Operation(operationId = "platformDictionaryTypeGet", summary = "查询平台字典类型")
    public R<DictionaryTypeVo> getDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        return R.ok(platformDictionaryService.getDictionaryType(dictionaryTypeId));
    }

    @Override
    @POST
    @Operation(operationId = "platformDictionaryTypeCreate", summary = "创建平台字典类型")
    public R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request) {
        return R.ok(platformDictionaryService.createDictionaryType(request));
    }

    @Override
    @PUT
    @Path("/{dictionaryTypeId}")
    @Operation(operationId = "platformDictionaryTypeUpdate", summary = "更新平台字典类型")
    public R<DictionaryTypeVo> updateDictionaryType(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeUpdateRequest request) {
        return R.ok(platformDictionaryService.updateDictionaryType(dictionaryTypeId, request));
    }

    @Override
    @PUT
    @Path("/{dictionaryTypeId}/status")
    @Operation(operationId = "platformDictionaryTypeUpdateStatus", summary = "更新平台字典类型状态")
    public R<Void> updateDictionaryTypeStatus(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeStatusUpdateRequest request) {
        platformDictionaryService.updateDictionaryTypeStatus(dictionaryTypeId, request);
        return R.ok();
    }

    @Override
    @DELETE
    @Path("/{dictionaryTypeId}")
    @Operation(operationId = "platformDictionaryTypeDelete", summary = "删除平台字典类型")
    public R<Void> deleteDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId) {
        platformDictionaryService.deleteDictionaryType(dictionaryTypeId);
        return R.ok();
    }

    @Override
    @GET
    @Path("/options")
    @Operation(operationId = "platformDictionaryTypeListOptions", summary = "平台字典类型下拉选项")
    public R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(@QueryParam("status") BasicStatus status) {
        return R.ok(platformDictionaryService.listDictionaryTypeOptions(status));
    }

    @Override
    @GET
    @Path("/{typeCode}/items")
    @Operation(operationId = "platformDictionaryItemPage", summary = "分页查询平台字典项")
    public R<PageResult<DictionaryItemVo>> pageDictionaryItems(
            @PathParam("typeCode") String typeCode,
            @BeanParam DictionaryItemPageRequest request) {
        return R.ok(platformDictionaryService.pageDictionaryItems(typeCode, request));
    }
}

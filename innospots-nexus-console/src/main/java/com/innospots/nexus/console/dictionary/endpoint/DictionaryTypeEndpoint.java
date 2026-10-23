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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeCreateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypePageRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeStatusUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.request.DictionaryTypeUpdateRequest;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeOptionVo;
import com.innospots.nexus.console.dictionary.domain.vo.DictionaryTypeVo;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 字典类型管理 REST 契约（字典项随类型详情与创建/更新请求体维护，无独立字典项资源）。
 *
 * <p>类级 {@code @Path} 由业务域实现（如 platform）绑定。实现类必须重复方法级
 * JAX-RS 与 OpenAPI 注解供构建期扫描；{@code @EndpointModule} / {@code @EndpointGrant}
 * 仍只在本接口声明。</p>
 */
@EndpointModule("dictionary")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Dictionary", description = "字典类型与字典项")
@NexusAuthenticatedApi
public interface DictionaryTypeEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "dictionary.type.page",
            label = @I18nLiteral(pairs = {"zh-CN", "分页查询字典类型", "en", "Page dictionary types"}))
    @Operation(operationId = "dictionaryTypePage", summary = "分页查询字典类型")
    R<PageResult<DictionaryTypeVo>> pageDictionaryTypes(@BeanParam DictionaryTypePageRequest request);

    @GET
    @Path("/{dictionaryTypeId}")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "dictionary.type.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询字典类型", "en", "Get dictionary type"}))
    @Operation(operationId = "dictionaryTypeGet", summary = "查询字典类型")
    R<DictionaryTypeVo> getDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId);

    @POST
    @EndpointGrant(
            action = EndpointGrantAction.CREATE,
            name = "dictionary.type.create",
            label = @I18nLiteral(pairs = {"zh-CN", "创建字典类型", "en", "Create dictionary type"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "dictionaryTypeCreate", summary = "创建字典类型")
    R<DictionaryTypeVo> createDictionaryType(DictionaryTypeCreateRequest request);

    @PUT
    @Path("/{dictionaryTypeId}")
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "dictionary.type.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新字典类型", "en", "Update dictionary type"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "dictionaryTypeUpdate", summary = "更新字典类型")
    R<DictionaryTypeVo> updateDictionaryType(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeUpdateRequest request
    );

    @PUT
    @Path("/{dictionaryTypeId}/status")
    @EndpointGrant(
            action = EndpointGrantAction.STATUS,
            name = "dictionary.type.status.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新字典类型状态", "en", "Update dictionary type status"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "dictionaryTypeUpdateStatus", summary = "更新字典类型状态")
    R<Void> updateDictionaryTypeStatus(
            @PathParam("dictionaryTypeId") String dictionaryTypeId,
            DictionaryTypeStatusUpdateRequest request
    );

    @DELETE
    @Path("/{dictionaryTypeId}")
    @EndpointGrant(
            action = EndpointGrantAction.DELETE,
            name = "dictionary.type.delete",
            label = @I18nLiteral(pairs = {"zh-CN", "删除字典类型", "en", "Delete dictionary type"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "dictionaryTypeDelete", summary = "删除字典类型")
    R<Void> deleteDictionaryType(@PathParam("dictionaryTypeId") String dictionaryTypeId);

    @GET
    @Path("/options")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "dictionary.type.options",
            label = @I18nLiteral(pairs = {"zh-CN", "字典类型下拉选项", "en", "List dictionary type options"}),
            ignorePermission = true)
    @Operation(operationId = "dictionaryTypeListOptions", summary = "字典类型下拉选项")
    R<List<DictionaryTypeOptionVo>> listDictionaryTypeOptions(@QueryParam("status") BasicStatus status);
}

package com.innospots.nexus.console.role.endpoint;

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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.role.domain.request.RoleCreateRequest;
import com.innospots.nexus.console.role.domain.request.RolePageRequest;
import com.innospots.nexus.console.role.domain.request.RoleStatusUpdateRequest;
import com.innospots.nexus.console.role.domain.request.RoleUpdateRequest;
import com.innospots.nexus.console.role.domain.vo.RoleOptionVo;
import com.innospots.nexus.console.role.domain.vo.RoleVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 角色生命周期与查询 REST 契约。
 *
 * <p>仅定义方法级路由与载荷形状；类级 {@code @Path} 由业务域实现（如 platform）绑定。
 * 创建请求不含 {@code ownerType}/{@code ownerId}，由实现侧按域固化归属。</p>
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Role", description = "角色与绑定")
@NexusAuthenticatedApi
public interface RoleEndpoint {

    @GET
    @Operation(operationId = "rolePage", summary = "分页查询角色")
    R<PageResult<RoleVo>> pageRoles(@BeanParam RolePageRequest request);

    @GET
    @Path("/{roleId}")
    @Operation(operationId = "roleGet", summary = "查询角色详情")
    R<RoleVo> getRole(
            @Parameter(description = "角色 ID", required = true) @PathParam("roleId") String roleId);

    @POST
    @Operation(operationId = "roleCreate", summary = "创建角色")
    R<RoleVo> createRole(RoleCreateRequest request);

    @PUT
    @Path("/{roleId}")
    @Operation(operationId = "roleUpdate", summary = "更新角色")
    R<RoleVo> updateRole(
            @PathParam("roleId") String roleId,
            RoleUpdateRequest request);

    @PUT
    @Path("/{roleId}/status")
    @Operation(operationId = "roleUpdateStatus", summary = "更新角色状态")
    R<Void> updateRoleStatus(
            @PathParam("roleId") String roleId,
            RoleStatusUpdateRequest request);

    @DELETE
    @Path("/{roleId}")
    @Operation(operationId = "roleDelete", summary = "删除角色")
    R<Void> deleteRole(@PathParam("roleId") String roleId);

    @GET
    @Path("/options")
    @Operation(operationId = "roleListOptions", summary = "角色下拉选项")
    R<List<RoleOptionVo>> listRoleOptions(@QueryParam("status") BasicStatus status);
}

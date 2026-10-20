package com.innospots.nexus.console.role.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;
import com.innospots.nexus.console.role.domain.request.RoleBindingAddRequest;
import com.innospots.nexus.console.role.domain.request.RoleBindingPageRequest;
import com.innospots.nexus.console.role.domain.vo.RoleBindingVo;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;

/**
 * 角色绑定 REST 契约。
 *
 * <p>仅定义方法级路由；类级 {@code @Path} 由业务域实现绑定（如 platform）。</p>
 */
@EndpointModule("role")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Role", description = "角色与绑定")
@NexusAuthenticatedApi
public interface RoleBindingEndpoint {

    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "role.binding.page",
            label = @I18nLiteral(pairs = {"zh-CN", "分页查询角色绑定", "en", "Page role bindings"}))
    @Operation(operationId = "roleBindingPage", summary = "分页查询角色绑定")
    R<PageResult<RoleBindingVo>> pageRoleBindings(
            @PathParam("roleId") String roleId,
            @BeanParam RoleBindingPageRequest request);

    @POST
    @EndpointGrant(
            action = EndpointGrantAction.ASSIGN,
            name = "role.binding.add",
            label = @I18nLiteral(pairs = {"zh-CN", "新增角色绑定", "en", "Add role bindings"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "roleBindingAdd", summary = "新增角色绑定")
    R<Void> addRoleBindings(
            @PathParam("roleId") String roleId,
            RoleBindingAddRequest request);

    @DELETE
    @Path("/{bindingId}")
    @EndpointGrant(
            action = EndpointGrantAction.REVOKE,
            name = "role.binding.remove",
            label = @I18nLiteral(pairs = {"zh-CN", "移除角色绑定", "en", "Remove role binding"}),
            recordArgs = false,
            auditLog = true)
    @Operation(operationId = "roleBindingRemove", summary = "移除角色绑定")
    R<Void> removeRoleBinding(
            @PathParam("roleId") String roleId,
            @PathParam("bindingId") String bindingId);
}

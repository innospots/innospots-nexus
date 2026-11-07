package com.innospots.nexus.platform.user.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserCreateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPageRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPasswordResetRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserStatusUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.vo.AdminUserVo;
import com.innospots.nexus.console.user.admin.endpoint.AdminUserEndpoint;
import com.innospots.nexus.console.user.admin.service.AdminUserService;
import com.innospots.nexus.core.endpoint.grant.EndpointGrant;
import com.innospots.nexus.core.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.core.endpoint.grant.EndpointModule;
import com.innospots.nexus.core.endpoint.grant.I18nLiteral;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.entry.BuiltinPlatformEntryPlugins;

/**
 * 运营侧用户管理 REST 资源。
 *
 * <p>契约由 console 的 {@link AdminUserEndpoint} 定义；本类只绑定运营平台的路径、
 * 权限组与实现。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@EndpointModule(value = "user", domain = BuiltinPlatformEntryPlugins.OPERATION_DOMAIN_KEY)
@Path(PlatformConstant.USERS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "AdminUser", description = "平台用户管理")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformAdminUserEndpoint implements AdminUserEndpoint {

    private final AdminUserService adminUserService;

    @Override
    @GET
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.user.page",
            label = @I18nLiteral(pairs = {"zh-CN", "分页查询平台用户", "en", "Page platform users"}))
    public R<PageResult<AdminUserVo>> pageUsers(@BeanParam AdminUserPageRequest request) {
        return R.ok(adminUserService.pageUsers(request));
    }

    @Override
    @POST
    @EndpointGrant(
            action = EndpointGrantAction.CREATE,
            name = "platform.user.create",
            label = @I18nLiteral(pairs = {"zh-CN", "创建平台用户", "en", "Create platform user"}),
            recordArgs = false,
            auditLog = true)
    public R<AdminUserVo> createUser(AdminUserCreateRequest request) {
        return R.ok(adminUserService.createUser(request));
    }

    @Override
    @GET
    @Path("/{adminUserId}")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "platform.user.get",
            label = @I18nLiteral(pairs = {"zh-CN", "查询平台用户", "en", "Get platform user"}))
    public R<AdminUserVo> getUser(@PathParam("adminUserId") String adminUserId) {
        return R.ok(adminUserService.getUser(adminUserId));
    }

    @Override
    @PUT
    @Path("/{adminUserId}")
    @EndpointGrant(
            action = EndpointGrantAction.UPDATE,
            name = "platform.user.update",
            label = @I18nLiteral(pairs = {"zh-CN", "更新平台用户资料", "en", "Update platform user"}),
            recordArgs = false,
            auditLog = true)
    public R<AdminUserVo> updateUser(
            @PathParam("adminUserId") String adminUserId,
            AdminUserUpdateRequest request) {
        return R.ok(adminUserService.updateUser(adminUserId, request));
    }

    @Override
    @PUT
    @Path("/{adminUserId}/status")
    @EndpointGrant(
            action = EndpointGrantAction.STATUS,
            name = "platform.user.status",
            label = @I18nLiteral(pairs = {"zh-CN", "更新平台用户状态", "en", "Update platform user status"}),
            recordArgs = false,
            auditLog = true)
    public R<Void> updateUserStatus(
            @PathParam("adminUserId") String adminUserId,
            AdminUserStatusUpdateRequest request) {
        adminUserService.updateUserStatus(adminUserId, request);
        return R.ok();
    }

    @Override
    @PUT
    @Path("/{adminUserId}/password")
    @EndpointGrant(
            action = EndpointGrantAction.EXECUTE,
            name = "platform.user.password.reset",
            label = @I18nLiteral(pairs = {"zh-CN", "管理员重置平台用户密码", "en", "Admin reset platform user password"}),
            recordArgs = false,
            auditLog = true)
    public R<Void> resetUserPassword(
            @PathParam("adminUserId") String adminUserId,
            AdminUserPasswordResetRequest request) {
        adminUserService.resetUserPassword(adminUserId, request);
        return R.ok();
    }
}

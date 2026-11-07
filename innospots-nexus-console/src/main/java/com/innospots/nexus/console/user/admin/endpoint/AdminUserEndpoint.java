package com.innospots.nexus.console.user.admin.endpoint;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserCreateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPageRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserPasswordResetRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserStatusUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.request.AdminUserUpdateRequest;
import com.innospots.nexus.console.user.admin.domain.vo.AdminUserVo;

/**
 * 运营侧用户管理 REST 契约。
 *
 * <p>类级 {@code @Path} 与 {@code @EndpointModule} 由实现方绑定：
 * 路径属运营平台，权限组属运营域，两者都是业务方决定，console 只定义契约。</p>
 *
 * <p>不暴露公开自助注册；账号由管理员创建。</p>
 *
 * @author Smars
 * @date 2026/11/07
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AdminUserEndpoint {

    /**
     * 分页查询用户。
     *
     * @param request 分页与过滤条件
     * @return 用户分页
     */
    @GET
    @Operation(operationId = "adminUserPage", summary = "分页查询用户")
    R<PageResult<AdminUserVo>> pageUsers(@BeanParam AdminUserPageRequest request);

    /**
     * 创建用户。
     *
     * @param request 创建请求
     * @return 新建用户
     */
    @POST
    @Operation(operationId = "adminUserCreate", summary = "创建用户")
    R<AdminUserVo> createUser(AdminUserCreateRequest request);

    /**
     * 查询单个用户。
     *
     * @param adminUserId 用户标识
     * @return 用户详情
     */
    @GET
    @Path("/{adminUserId}")
    @Operation(operationId = "adminUserGet", summary = "查询用户")
    R<AdminUserVo> getUser(
            @Parameter(description = "用户 ID", required = true)
            @PathParam("adminUserId") String adminUserId);

    /**
     * 更新用户资料。
     *
     * @param adminUserId 用户标识
     * @param request     更新请求
     * @return 更新后的用户
     */
    @PUT
    @Path("/{adminUserId}")
    @Operation(operationId = "adminUserUpdate", summary = "更新用户资料")
    R<AdminUserVo> updateUser(
            @Parameter(description = "用户 ID", required = true)
            @PathParam("adminUserId") String adminUserId,
            AdminUserUpdateRequest request);

    /**
     * 更新用户状态。
     *
     * @param adminUserId 用户标识
     * @param request     状态请求
     * @return 空响应
     */
    @PUT
    @Path("/{adminUserId}/status")
    @Operation(operationId = "adminUserUpdateStatus", summary = "更新用户状态（仅 ACTIVE 或 DISABLED）")
    R<Void> updateUserStatus(
            @Parameter(description = "用户 ID", required = true)
            @PathParam("adminUserId") String adminUserId,
            AdminUserStatusUpdateRequest request);

    /**
     * 管理员重置用户密码。
     *
     * @param adminUserId 用户标识
     * @param request     重置请求
     * @return 空响应
     */
    @PUT
    @Path("/{adminUserId}/password")
    @Operation(operationId = "adminUserResetPassword", summary = "管理员重置用户密码")
    R<Void> resetUserPassword(
            @Parameter(description = "用户 ID", required = true)
            @PathParam("adminUserId") String adminUserId,
            AdminUserPasswordResetRequest request);
}

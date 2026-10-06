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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.PageResult;
import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.core.openapi.NexusAuthenticatedApi;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserPageRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserStatusUpdateRequest;
import com.innospots.nexus.platform.user.domain.request.PlatformUserUpdateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;
import com.innospots.nexus.platform.user.service.PlatformUserService;

/**
 * 平台用户管理的运营管理平台 REST 资源。
 * <p>不暴露公开自助注册。账号由管理员创建。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path(PlatformConstant.USERS_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformUser", description = "平台用户管理")
@NexusAuthenticatedApi
@RequiredArgsConstructor
public class PlatformUserEndpoint {

    private final PlatformUserService platformUserService;

    @GET
    @Operation(operationId = "platformUserPage", summary = "分页查询平台用户")
    public R<PageResult<PlatformUserVo>> pageUsers(@BeanParam PlatformUserPageRequest request) {
        return R.ok(platformUserService.pageUsers(request));
    }

    @POST
    @Operation(operationId = "platformUserCreate", summary = "创建平台用户")
    public R<PlatformUserVo> createUser(PlatformUserCreateRequest request) {
        return R.ok(platformUserService.createUser(request));
    }

    @GET
    @Path("/{platformUserId}")
    @Operation(operationId = "platformUserGet", summary = "查询平台用户")
    public R<PlatformUserVo> getUser(
            @Parameter(description = "平台用户 ID", required = true) @PathParam("platformUserId") String platformUserId) {
        return R.ok(platformUserService.getUser(platformUserId));
    }

    @PUT
    @Path("/{platformUserId}")
    @Operation(operationId = "platformUserUpdate", summary = "更新平台用户资料")
    public R<PlatformUserVo> updateUser(
            @Parameter(description = "平台用户 ID", required = true) @PathParam("platformUserId") String platformUserId,
            PlatformUserUpdateRequest request) {
        return R.ok(platformUserService.updateUser(platformUserId, request));
    }

    @PUT
    @Path("/{platformUserId}/status")
    @Operation(operationId = "platformUserUpdateStatus", summary = "更新平台用户状态")
    public R<Void> updateUserStatus(
            @Parameter(description = "平台用户 ID", required = true) @PathParam("platformUserId") String platformUserId,
            PlatformUserStatusUpdateRequest request) {
        platformUserService.updateUserStatus(platformUserId, request);
        return R.ok();
    }
}

package com.innospots.nexus.platform.user.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.user.domain.request.PlatformUserCreateRequest;
import com.innospots.nexus.platform.user.domain.vo.PlatformUserVo;

/**
 * 平台用户管理的运维域契约。
 * <p>不暴露公开自助注册。账号由管理员创建。</p>
 *
 * @author Smars
 * @date 2026/09/13
 */
@Path("/platform/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PlatformUserEndpoint {

    /**
     * 使用本地密码创建平台用户。
     *
     * @param request admin create 请求
     * @return created user 概要
     */
    @POST
    R<PlatformUserVo> createUser(PlatformUserCreateRequest request);

    /**
     * 返回单个平台用户。
     *
     * @param platformUserId platform-realm user 标识符
     * @return user 概要
     */
    @GET
    @Path("/{platformUserId}")
    R<PlatformUserVo> getUser(@PathParam("platformUserId") String platformUserId);
}

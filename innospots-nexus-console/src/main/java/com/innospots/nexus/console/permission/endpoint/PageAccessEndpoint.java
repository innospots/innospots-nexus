package com.innospots.nexus.console.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;

/**
 * 页面访问权限校验 REST 契约（开放接口，不校验登录与功能授权）。
 *
 * <p>路径固定为 {@link ConsoleConstant#PUBLIC_API_PREFIX} + {@code /page-access}，
 * 该前缀下的请求不执行登录鉴权与页面权限校验。</p>
 *
 * <p>是否允许访问页面由请求体与可选会话主体在业务层判定。</p>
 */
@Path(ConsoleConstant.PUBLIC_API_PREFIX + "/page-access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
public interface PageAccessEndpoint {

    @POST
    @Path("verify")
    @Operation(operationId = "permissionPageAccessVerify", summary = "校验页面访问权限")
    R<PageAccessVerifyResult> verifyPageAccess(PageAccessVerifyRequest request);
}

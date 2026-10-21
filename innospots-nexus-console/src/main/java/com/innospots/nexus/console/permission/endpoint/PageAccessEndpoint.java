package com.innospots.nexus.console.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.endpoint.grant.EndpointGrant;
import com.innospots.nexus.console.endpoint.grant.EndpointGrantAction;
import com.innospots.nexus.console.endpoint.grant.EndpointModule;
import com.innospots.nexus.console.logger.I18nLiteral;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;

/**
 * 页面访问权限校验 REST 契约（开放接口，不校验登录与功能授权）；路径由 platform/portal 实现绑定。
 *
 * <p>是否允许访问页面由请求体与可选会话主体在业务层判定。</p>
 */
@EndpointModule(value = "permission", ignoreAuth = true)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Permission", description = "授权与可见资源")
public interface PageAccessEndpoint {

    @POST
    @Path("verify")
    @EndpointGrant(
            action = EndpointGrantAction.READ,
            name = "permission.pageAccess.verify",
            label = @I18nLiteral(pairs = {"zh-CN", "校验页面访问权限", "en", "Verify page access"}),
            ignoreAuth = true,
            ignorePermission = true)
    @Operation(operationId = "permissionPageAccessVerify", summary = "校验页面访问权限")
    R<PageAccessVerifyResult> verifyPageAccess(PageAccessVerifyRequest request);
}

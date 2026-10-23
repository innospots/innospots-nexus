package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.config.ConsoleConstant;
import com.innospots.nexus.console.permission.endpoint.PageAccessEndpoint;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;
import com.innospots.nexus.platform.permission.service.PlatformPageAccessService;

@Path(ConsoleConstant.PUBLIC_API_PREFIX + "/page-access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@RequiredArgsConstructor
public class PlatformPageAccessEndpoint implements PageAccessEndpoint {

    private final PlatformPageAccessService platformPageAccessService;

    @Override
    @POST
    @Path("verify")
    @Operation(operationId = "platformPermissionPageAccessVerify", summary = "校验页面访问权限")
    public R<PageAccessVerifyResult> verifyPageAccess(PageAccessVerifyRequest request) {
        return R.ok(platformPageAccessService.verify(request));
    }
}

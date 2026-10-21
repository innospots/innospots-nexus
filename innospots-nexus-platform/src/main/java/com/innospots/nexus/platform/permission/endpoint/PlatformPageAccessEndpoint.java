package com.innospots.nexus.platform.permission.endpoint;

import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.permission.endpoint.PageAccessEndpoint;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyRequest;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifyResult;
import com.innospots.nexus.platform.config.PlatformConstant;
import com.innospots.nexus.platform.permission.service.PlatformPageAccessService;

@Path(PlatformConstant.PAGE_ACCESS_VERIFY_PATH)
@Tag(name = "PlatformPermission", description = "运营平台授权")
@RequiredArgsConstructor
public class PlatformPageAccessEndpoint implements PageAccessEndpoint {

    private final PlatformPageAccessService platformPageAccessService;

    @Override
    public R<PageAccessVerifyResult> verifyPageAccess(PageAccessVerifyRequest request) {
        return R.ok(platformPageAccessService.verify(request));
    }
}

package com.innospots.nexus.platform.access.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.platform.access.domain.request.PlatformOpenRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * OPEN 模式公开注册 REST（{@link PlatformConstant#PUBLIC_OPEN_REGISTRATION_PATH}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.service.PlatformOpenRegistrationService
 */
@Path(PlatformConstant.PUBLIC_OPEN_REGISTRATION_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicOpenRegistration", description = "完全开放注册")
@RequiredArgsConstructor
public class PlatformPublicOpenRegistrationEndpoint {

    private final PlatformOpenRegistrationService openRegistrationService;

    @POST
    @Path("/otp")
    @Operation(operationId = "platformPublicOpenRegistrationOtp", summary = "开放注册获取验证码")
    public R<Void> issueVerificationOtp(PlatformRegistrationOtpRequest request) {
        openRegistrationService.issueVerificationOtp(request);
        return R.ok();
    }

    @POST
    @Operation(operationId = "platformPublicOpenRegistrationSubmit", summary = "提交开放注册")
    public R<String> register(PlatformOpenRegistrationSubmitRequest request) {
        return R.ok(openRegistrationService.register(request));
    }
}

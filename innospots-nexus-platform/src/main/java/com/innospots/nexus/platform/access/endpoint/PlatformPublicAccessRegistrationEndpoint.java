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
import com.innospots.nexus.platform.access.domain.request.PlatformAccessRegistrationSubmitRequest;
import com.innospots.nexus.platform.access.domain.request.PlatformRegistrationOtpRequest;
import com.innospots.nexus.platform.access.domain.vo.PlatformAccessRequestVo;
import com.innospots.nexus.platform.access.service.PlatformAccessRequestService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * APPROVAL 模式公开注册 REST（{@link PlatformConstant#PUBLIC_ACCESS_REGISTRATION_PATH}）。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.access.service.PlatformAccessRequestService#submitAccessRegistration
 */
@Path(PlatformConstant.PUBLIC_ACCESS_REGISTRATION_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicAccessRegistration", description = "主动注册（待审批）")
@RequiredArgsConstructor
public class PlatformPublicAccessRegistrationEndpoint {

    private final PlatformAccessRequestService accessRequestService;

    @POST
    @Path("/otp")
    @Operation(operationId = "platformPublicAccessRegistrationOtp", summary = "主动注册获取验证码")
    public R<Void> issueVerificationOtp(PlatformRegistrationOtpRequest request) {
        accessRequestService.issueAccessRegistrationOtp(request);
        return R.ok();
    }

    @POST
    @Operation(operationId = "platformPublicAccessRegistrationSubmit", summary = "提交主动注册申请")
    public R<PlatformAccessRequestVo> submitRegistration(PlatformAccessRegistrationSubmitRequest request) {
        return R.ok(accessRequestService.submitAccessRegistration(request));
    }
}

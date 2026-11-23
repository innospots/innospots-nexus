package com.innospots.nexus.portal.registration.endpoint;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.user.portal.domain.vo.PortalUserProfileVo;
import com.innospots.nexus.portal.registration.domain.request.TenantRegisterRequest;
import com.innospots.nexus.portal.registration.service.TenantRegistrationFacade;

/**
 * 租户域身份注册 REST 资源。
 *
 * <p>注册与认证分离：本端点只创建身份，不返回令牌；登录请使用
 * {@link com.innospots.nexus.portal.auth.endpoint.TenantAuthEndpoint}。</p>
 */
@Path("/tenant/registration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "TenantRegistration", description = "租户域身份注册")
@RequiredArgsConstructor
public class TenantRegistrationEndpoint {

    private final TenantRegistrationFacade registrationFacade;

    @POST
    @Path("/register")
    @Operation(operationId = "tenantRegistrationRegister", summary = "注册租户域身份")
    public R<PortalUserProfileVo> register(TenantRegisterRequest request) {
        return R.ok(registrationFacade.register(request));
    }
}

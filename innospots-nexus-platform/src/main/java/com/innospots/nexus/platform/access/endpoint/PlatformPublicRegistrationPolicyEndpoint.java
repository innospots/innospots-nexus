package com.innospots.nexus.platform.access.endpoint;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.innospots.nexus.base.domain.response.R;
import com.innospots.nexus.console.auth.domain.vo.PublicRegistrationPolicyVo;
import com.innospots.nexus.console.auth.service.RegistrationPolicyService;
import com.innospots.nexus.platform.config.PlatformConstant;

/**
 * 公开注册策略（匿名），供登录/注册页按 {@link com.innospots.nexus.console.config.RegistrationConfig}
 * 渲染表单与路由门禁。
 *
 * @author Smars
 * @date 2026/10/18
 * @see PlatformConstant#PUBLIC_REGISTRATION_POLICY_PATH
 * @see RegistrationPolicyService#getPublicPolicy()
 */
@Path(PlatformConstant.PUBLIC_REGISTRATION_POLICY_PATH)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "PlatformPublicRegistrationPolicy", description = "公开注册策略")
@RequiredArgsConstructor
public class PlatformPublicRegistrationPolicyEndpoint {

    /** 读取部署期注册配置并生成公开 VO。 */
    private final RegistrationPolicyService registrationPolicyService;

    @GET
    @Operation(operationId = "platformPublicRegistrationPolicyGet", summary = "查询当前注册策略")
    public R<PublicRegistrationPolicyVo> getPolicy() {
        return R.ok(registrationPolicyService.getPublicPolicy());
    }
}

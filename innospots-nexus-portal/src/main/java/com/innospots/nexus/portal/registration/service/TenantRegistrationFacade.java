package com.innospots.nexus.portal.registration.service;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.user.portal.domain.request.PortalUserPasswordRegisterRequest;
import com.innospots.nexus.console.user.portal.domain.vo.PortalUserProfileVo;
import com.innospots.nexus.console.user.portal.service.PortalUserService;
import com.innospots.nexus.portal.registration.domain.request.TenantRegisterRequest;

/**
 * 租户域身份注册编排。
 *
 * <p>注册只创建登录身份与密码凭证，不签发令牌；登录仍由 auth 领域完成。</p>
 */
@RequiredArgsConstructor
public class TenantRegistrationFacade {

    private final PortalUserService portalUserService;

    /**
     * 注册租户域身份。
     *
     * @param request 注册请求（密码已由前端 RSA 加密）
     * @return 已创建的用户档案
     */
    public PortalUserProfileVo register(TenantRegisterRequest request) {
        Checks.notNull(request, "request");
        Checks.notBlank(request.userName(), "userName");
        Checks.notBlank(request.encryptedPassword(), "encryptedPassword");
        return portalUserService.registerWithPassword(new PortalUserPasswordRegisterRequest(
                request.userName(),
                request.displayName(),
                request.email(),
                request.mobile(),
                request.region(),
                request.timeZone(),
                request.language(),
                request.encryptedPassword()));
    }
}

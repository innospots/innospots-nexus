package com.innospots.nexus.platform.permission.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;
import com.innospots.nexus.platform.scope.PlatformConsoleSession.Context;

/**
 * 运营平台权限服务：PLATFORM 会话与归属固化，委托 console 授权组件。
 */
@RequiredArgsConstructor
public class PlatformPermissionService {

    private final PermissionGrantService grantService;
    private final AuthorizationSubjectResolver subjectResolver;
    private final PermissionVisibilityService visibilityService;

    public PermissionGrantReplaceRequest listRolePermissions(String roleId) {
        Checks.notBlank(roleId, "roleId");
        Context session = PlatformConsoleSession.requireContext();
        return grantService.list(
                PermissionSubjectType.ROLE,
                roleId,
                session.ownership(),
                session.tenantId(),
                session.workspaceId());
    }

    public void replaceRolePermissions(String roleId, PermissionGrantReplaceRequest request) {
        Checks.notBlank(roleId, "roleId");
        Checks.notNull(request, "request");
        Context session = PlatformConsoleSession.requireContext();
        grantService.replace(
                PermissionSubjectType.ROLE,
                roleId,
                request,
                session.ownership(),
                session.tenantId(),
                session.workspaceId());
    }

    public PermissionGrantReplaceRequest listOrganizationUnitPermissions(String unitId) {
        Checks.notBlank(unitId, "unitId");
        Context session = PlatformConsoleSession.requireContext();
        return grantService.list(
                PermissionSubjectType.ORG_UNIT,
                unitId,
                session.ownership(),
                session.tenantId(),
                session.workspaceId());
    }

    public void replaceOrganizationUnitPermissions(String unitId, PermissionGrantReplaceRequest request) {
        Checks.notBlank(unitId, "unitId");
        Checks.notNull(request, "request");
        Context session = PlatformConsoleSession.requireContext();
        grantService.replace(
                PermissionSubjectType.ORG_UNIT,
                unitId,
                request,
                session.ownership(),
                session.tenantId(),
                session.workspaceId());
    }

    public List<PermissionResourceVo> listCurrentUserResources() {
        Context session = PlatformConsoleSession.requireContext();
        return subjectResolver.resolve()
                .map(subject -> visibilityService.visible(session.ownership(), subject).stream()
                        .map(PermissionResourceVo::from)
                        .toList())
                .orElseGet(List::of);
    }
}

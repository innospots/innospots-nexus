package com.innospots.nexus.platform.permission.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.permission.domain.vo.PermissionResourceVo;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.platform.scope.PlatformConsoleSession;
import com.innospots.nexus.platform.support.PlatformMutationLog;

/**
 * 运营平台权限服务：PLATFORM 会话与归属固化，委托 console 授权组件。
 */
@Slf4j
@RequiredArgsConstructor
public class PlatformPermissionService {

    private final PermissionGrantService grantService;
    private final AuthorizationSubjectResolver subjectResolver;
    private final PermissionVisibilityService visibilityService;

    public PermissionGrantReplaceRequest listRolePermissions(String roleId) {
        Checks.notBlank(roleId, "roleId");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return grantService.list(PermissionSubjectType.ROLE, roleId, ownership);
    }

    public void replaceRolePermissions(String roleId, PermissionGrantReplaceRequest request) {
        Checks.notBlank(roleId, "roleId");
        Checks.notNull(request, "request");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        grantService.replace(PermissionSubjectType.ROLE, roleId, request, ownership);
        int grantCount = request.grants() == null ? 0 : request.grants().size();
        PlatformMutationLog.info(
                log,
                "role permissions replaced roleId={} grantCount={}",
                roleId,
                grantCount);
    }

    public PermissionGrantReplaceRequest listOrganizationUnitPermissions(String unitId) {
        Checks.notBlank(unitId, "unitId");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return grantService.list(PermissionSubjectType.ORG_UNIT, unitId, ownership);
    }

    public void replaceOrganizationUnitPermissions(String unitId, PermissionGrantReplaceRequest request) {
        Checks.notBlank(unitId, "unitId");
        Checks.notNull(request, "request");
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        grantService.replace(PermissionSubjectType.ORG_UNIT, unitId, request, ownership);
        int grantCount = request.grants() == null ? 0 : request.grants().size();
        PlatformMutationLog.info(
                log,
                "organization unit permissions replaced unitId={} grantCount={}",
                unitId,
                grantCount);
    }

    public List<PermissionResourceVo> listCurrentUserResources() {
        ConsoleOwnership ownership = PlatformConsoleSession.requireOwnership();
        return subjectResolver.resolve()
                .map(subject -> visibilityService.visible(ownership, subject).stream()
                        .map(PermissionResourceVo::from)
                        .toList())
                .orElseGet(List::of);
    }
}

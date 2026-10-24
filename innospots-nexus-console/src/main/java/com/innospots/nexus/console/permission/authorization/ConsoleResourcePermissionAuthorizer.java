package com.innospots.nexus.console.permission.authorization;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.model.CatalogGrantKeys;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;

/**
 * 控制台资源授权判定（catalog v2：按 {@link CatalogGrantKeys} 资源键匹配 {@code nx_permission_grant}）。
 *
 * <p>HTTP 请求侧由 Filter 传入 {@link ResolvedEndpointGrant}，拼出
 * {@code EndpointGrant.name:domainKey.moduleKey} 后查授权表；页面访问校验则直接传入已拼好的资源键。</p>
 */
public final class ConsoleResourcePermissionAuthorizer {

    private final ConsoleCatalogResourceDao resourceDao;
    private final PermissionGrantDao grantDao;

    public ConsoleResourcePermissionAuthorizer(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao
    ) {
        this.resourceDao = resourceDao;
        this.grantDao = grantDao;
    }

    /**
     * 按已解析的 endpoint grant 拼装资源键并判定是否已授权。
     *
     * @param workspaceId 当前 workspace
     * @param grant       请求方法上的 {@link ResolvedEndpointGrant}
     * @param subject     当前主体
     * @return 鉴权结果
     */
    public AuthorizationDecision authorizeEndpoint(
            String workspaceId,
            ResolvedEndpointGrant grant,
            AuthorizationSubject subject
    ) {
        if (workspaceId == null || grant == null || subject == null
                || blank(grant.name()) || blank(grant.moduleKey())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String domainKey = domainKeyByModuleKey(grant.moduleKey().trim());
        if (blank(domainKey)) {
            return AuthorizationDecision.deny("Module is not available");
        }
        String resourceKey = CatalogGrantKeys.of(
                grant.name().trim(),
                domainKey,
                grant.moduleKey().trim());
        if (blank(resourceKey)) {
            return AuthorizationDecision.deny("Resource is not grantable");
        }
        return authorize(new AuthorizationRequest(workspaceId, resourceKey, subject));
    }

    /**
     * 按资源键判定当前主体是否已授权。
     *
     * @param request 含 workspace、资源键与主体
     * @return 鉴权结果
     */
    public AuthorizationDecision authorize(AuthorizationRequest request) {
        if (request == null || request.workspaceId() == null || request.subject() == null
                || blank(request.resourceKey())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String resourceKey = request.resourceKey().trim();
        if (!request.subject().administrator()
                && !hasGrant(request.workspaceId(), resourceKey, request.subject())) {
            return AuthorizationDecision.deny("Resource permission is required");
        }
        return AuthorizationDecision.allow(new AuthorizationContext(
                request.workspaceId(), resourceKey, null, List.of()));
    }

    private String domainKeyByModuleKey(String moduleKey) {
        List<ConsoleCatalogResourceEntity> modules = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getResourceType,
                                CatalogResourceType.MODULE.name())
                        .eq(ConsoleCatalogResourceEntity::getModuleKey, moduleKey)
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        if (modules.size() != 1) {
            return null;
        }
        return modules.getFirst().getDomainKey();
    }

    private List<PermissionGrantEntity> grants(
            String workspaceId,
            String grantKey,
            AuthorizationSubject subject
    ) {
        List<PermissionGrantEntity> result = new ArrayList<>();
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership(workspaceId, null);
        if (!subject.roleIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.roleIds())
                            .eq(PermissionGrantEntity::getGrantKey, grantKey),
                    ownership)));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds())
                            .eq(PermissionGrantEntity::getGrantKey, grantKey),
                    ownership)));
        }
        return result;
    }

    private boolean hasGrant(String workspaceId, String grantKey, AuthorizationSubject subject) {
        return !grants(workspaceId, grantKey, subject).isEmpty();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

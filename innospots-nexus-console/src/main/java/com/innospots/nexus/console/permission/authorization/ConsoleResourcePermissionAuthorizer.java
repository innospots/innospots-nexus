package com.innospots.nexus.console.permission.authorization;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import com.innospots.nexus.console.scope.ConsoleSessionContext;
import com.innospots.nexus.console.scope.ConsoleSessionScope;
import com.innospots.nexus.core.endpoint.grant.support.ResolvedEndpointGrant;

/**
 * 控制台资源授权判定（catalog v2：按 {@link CatalogGrantKeys} 资源键匹配 {@code nx_permission_grant}）。
 *
 * <p>HTTP 请求侧由 Filter 传入 {@link ResolvedEndpointGrant}，拼出操作键
 * {@code domainKey.moduleKey:name()} 与分组键 {@code domainKey.moduleKey:grantGroup()}，
 * 授权表命中其一即通过。</p>
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
        if (grant == null || subject == null || blank(grant.moduleKey())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM && blank(workspaceId)) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String domainKey = resolveDomainKey(grant);
        if (blank(domainKey)) {
            return AuthorizationDecision.deny("Module is not available");
        }
        String moduleKey = grant.moduleKey().trim();
        String actionResourceKey = CatalogGrantKeys.of(grant.name(), domainKey, moduleKey);
        String groupResourceKey = CatalogGrantKeys.of(grant.grantGroup(), domainKey, moduleKey);
        List<String> resourceKeys = new ArrayList<>(2);
        if (!blank(actionResourceKey)) {
            resourceKeys.add(actionResourceKey);
        }
        if (!blank(groupResourceKey) && !groupResourceKey.equals(actionResourceKey)) {
            resourceKeys.add(groupResourceKey);
        }
        if (resourceKeys.isEmpty()) {
            return AuthorizationDecision.deny("Resource is not grantable");
        }
        return authorize(new AuthorizationRequest(workspaceId, resourceKeys, subject));
    }

    /**
     * 按资源键判定当前主体是否已授权。
     *
     * @param request 含 workspace、资源键候选与主体
     * @return 鉴权结果
     */
    public AuthorizationDecision authorize(AuthorizationRequest request) {
        if (request == null || request.subject() == null) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        if (ConsoleSessionContext.scope() != ConsoleSessionScope.PLATFORM && blank(request.workspaceId())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        List<String> resourceKeys = normalizeResourceKeys(request.resourceKeys());
        if (resourceKeys.isEmpty()) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String matchedKey = null;
        if (!request.subject().administrator()) {
            matchedKey = findMatchedGrantKey(request.workspaceId(), resourceKeys, request.subject());
            if (matchedKey == null) {
                return AuthorizationDecision.deny("Resource permission is required");
            }
        } else {
            matchedKey = resourceKeys.getFirst();
        }
        return AuthorizationDecision.allow(new AuthorizationContext(
                request.workspaceId(), matchedKey, null, List.of()));
    }

    private String resolveDomainKey(ResolvedEndpointGrant grant) {
        if (!blank(grant.domainKey())) {
            return grant.domainKey().trim();
        }
        return domainKeyByModuleKey(grant.moduleKey().trim());
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

    private String findMatchedGrantKey(
            String workspaceId,
            List<String> grantKeys,
            AuthorizationSubject subject
    ) {
        List<PermissionGrantEntity> rows = grants(workspaceId, grantKeys, subject);
        if (rows.isEmpty()) {
            return null;
        }
        return rows.getFirst().getGrantKey();
    }

    private List<PermissionGrantEntity> grants(
            String workspaceId,
            List<String> grantKeys,
            AuthorizationSubject subject
    ) {
        List<PermissionGrantEntity> result = new ArrayList<>();
        ConsoleOwnership ownership = ConsoleOwnershipScope.grantQueryOwnership(workspaceId);
        if (!subject.roleIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.roleIds())
                            .in(PermissionGrantEntity::getGrantKey, grantKeys),
                    ownership)));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds())
                            .in(PermissionGrantEntity::getGrantKey, grantKeys),
                    ownership)));
        }
        return result;
    }

    private static List<String> normalizeResourceKeys(List<String> resourceKeys) {
        if (resourceKeys == null || resourceKeys.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String key : resourceKeys) {
            if (!blank(key)) {
                unique.add(key.trim());
            }
        }
        return List.copyOf(unique);
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

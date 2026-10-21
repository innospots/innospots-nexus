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

/**
 * 控制台页面访问鉴权（catalog v2：叶子 MODULE 或 MENU 入口 + 授权记录）。
 */
public final class ConsolePagePermissionAuthorizer {

    private final ConsoleCatalogResourceDao resourceDao;
    private final PermissionGrantDao grantDao;

    public ConsolePagePermissionAuthorizer(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao
    ) {
        this.resourceDao = resourceDao;
        this.grantDao = grantDao;
    }

    public AuthorizationDecision authorize(AuthorizationRequest request) {
        if (request == null || request.workspaceId() == null || request.subject() == null
                || blank(request.pageKey())) {
            return AuthorizationDecision.deny("Invalid authorization request");
        }
        String pageKey = request.pageKey().trim();
        ConsoleCatalogResourceEntity pageResource = pageResourceByEntryPageKey(pageKey);
        if (pageResource == null) {
            return AuthorizationDecision.deny("Page is not available");
        }
        String grantKey = CatalogGrantKeys.viewKey(pageResource);
        if (blank(grantKey)) {
            return AuthorizationDecision.deny("Page is not grantable");
        }
        if (!request.subject().administrator()
                && !hasGrant(request.workspaceId(), grantKey, request.subject())) {
            return AuthorizationDecision.deny("Page permission is required");
        }
        return AuthorizationDecision.allow(new AuthorizationContext(
                request.workspaceId(), pageKey, null, List.of()));
    }

    private ConsoleCatalogResourceEntity pageResourceByEntryPageKey(String entryPageKey) {
        List<ConsoleCatalogResourceEntity> resources = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .in(ConsoleCatalogResourceEntity::getResourceType,
                                CatalogResourceType.MODULE.name(),
                                CatalogResourceType.MENU.name())
                        .eq(ConsoleCatalogResourceEntity::getEntryPageKey, entryPageKey)
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        return resources.size() == 1 ? resources.getFirst() : null;
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

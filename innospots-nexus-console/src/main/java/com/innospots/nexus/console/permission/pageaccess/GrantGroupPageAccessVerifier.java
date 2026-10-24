package com.innospots.nexus.console.permission.pageaccess;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.console.catalog.domain.model.CatalogGrantKeys;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 基于浏览分组授权键 {@code domainKey.moduleKey:grantGroup} 的页面访问校验。
 */
public final class GrantGroupPageAccessVerifier implements PageAccessVerifier {

    private final PermissionGrantDao grantDao;

    public GrantGroupPageAccessVerifier(PermissionGrantDao grantDao) {
        if (grantDao == null) {
            throw new IllegalArgumentException("grantDao is required");
        }
        this.grantDao = grantDao;
    }

    @Override
    public PageAccessVerifyResult verify(
            PageAccessVerifyRequest request,
            String workspaceId,
            AuthorizationSubject subject
    ) {
        if (request == null || blank(request.domainKey()) || blank(request.moduleKey())
                || blank(request.pageKey())) {
            return PageAccessVerifyResult.deny("Invalid page access request");
        }
        if (subject == null) {
            return PageAccessVerifyResult.deny("Authentication is required");
        }
        String grantGroup = request.grantGroup();
        if (blank(grantGroup)) {
            return PageAccessVerifyResult.deny("Grant group is required");
        }
        if (subject.administrator()) {
            return PageAccessVerifyResult.allow();
        }
        String viewKey = CatalogGrantKeys.viewGroupKey(
                request.domainKey().trim(),
                request.moduleKey().trim(),
                grantGroup.trim());
        if (blank(viewKey) || !hasViewGroupGrant(workspaceId, viewKey, subject)) {
            return PageAccessVerifyResult.deny("Page permission is required");
        }
        return PageAccessVerifyResult.allow();
    }

    private boolean hasViewGroupGrant(String workspaceId, String viewKey, AuthorizationSubject subject) {
        for (PermissionGrantEntity grant : grants(workspaceId, subject)) {
            if (viewKey.equals(grant.getGrantKey())) {
                return true;
            }
        }
        return false;
    }

    private List<PermissionGrantEntity> grants(String workspaceId, AuthorizationSubject subject) {
        List<PermissionGrantEntity> result = new ArrayList<>();
        ConsoleOwnership ownership = ConsoleOwnershipScope.workspaceOwnership(workspaceId, null);
        if (!subject.roleIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.roleIds()),
                    ownership)));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            result.addAll(grantDao.selectList(ConsoleOwnershipScope.apply(
                    Wrappers.<PermissionGrantEntity>lambdaQuery()
                            .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                            .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds()),
                    ownership)));
        }
        return result;
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}

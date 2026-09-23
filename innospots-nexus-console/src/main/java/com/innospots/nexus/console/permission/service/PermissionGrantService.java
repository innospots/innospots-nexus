package com.innospots.nexus.console.permission.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantItemRequest;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipGuard;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 管理角色和组织单元授权，并以全量替换方式保存授权结果。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PermissionGrantService {

    private final PermissionGrantDao grantDao;
    private final ConsoleCatalogResourceDao resourceDao;
    private final GrantSubjectAccess grantSubjectAccess;

    /**
     * 创建授权服务。
     */
    public PermissionGrantService(
            PermissionGrantDao grantDao,
            ConsoleCatalogResourceDao resourceDao,
            GrantSubjectAccess grantSubjectAccess
    ) {
        this.grantDao = Checks.notNull(grantDao, "grantDao");
        this.resourceDao = Checks.notNull(resourceDao, "resourceDao");
        this.grantSubjectAccess = Checks.notNull(grantSubjectAccess, "grantSubjectAccess");
    }

    /**
     * 在一个事务中全量替换指定角色或组织单元的授权。
     *
     * <p>方法先完成主体、资源、父子关系和 datasource 条件校验，再删除旧授权并写入新授权，
     * 从而避免部分校验失败时留下不完整的授权集合。</p>
     *
     * @param subjectType 授权主体类型
     * @param subjectId 角色或组织单元 ID
     * @param request 主体最终应拥有的完整授权集合
     */
    @Transactional
    public void replace(
            PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request
    ) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        Checks.notNull(request, "request");
        validateSubject(subjectType, subjectId, request);
        grantSubjectAccess.ensureSubjectInScope(subjectType, subjectId);
        List<ConsoleCatalogResourceEntity> resources = resources(request.grants());
        validateParents(resources);
        // 全量替换保证撤销的资源不会残留，同时事务保证删除和新增一起提交。
        grantDao.delete(grantScopeQuery(subjectType, subjectId, ownership));
        for (int i = 0; i < request.grants().size(); i++) {
            PermissionGrantItemRequest item = request.grants().get(i);
            ConsoleCatalogResourceEntity resource = resources.get(i);
            PermissionGrantEntity grant = new PermissionGrantEntity();
            ConsoleOwnershipScope.stamp(grant, ownership);
            grant.setSubjectType(subjectType.name());
            grant.setSubjectId(subjectId);
            grant.setResourceId(resource.getResourceId());
            grant.setConstraintDefinition(normalizeConstraint(
                    item.constraintDefinition(), resource));
            grantDao.insert(grant);
        }
    }

    /**
     * 查询指定角色或组织单元当前的完整授权集合。
     *
     * @param subjectType 授权主体类型
     * @param subjectId 角色或组织单元 ID
     * @return 当前授权及 datasource 附加查询条件
     */
    public PermissionGrantReplaceRequest list(
            PermissionSubjectType subjectType,
            String subjectId
    ) {
        ConsoleOwnership ownership = ConsoleOwnershipGuard.requireWorkspaceScope();
        validateSubject(subjectType, subjectId, null);
        grantSubjectAccess.ensureSubjectInScope(subjectType, subjectId);
        List<PermissionGrantItemRequest> grants = grantDao.selectList(grantScopeQuery(subjectType, subjectId, ownership))
                .stream()
                .map(grant -> new PermissionGrantItemRequest(
                        grant.getResourceId(), grant.getConstraintDefinition()))
                .toList();
        return new PermissionGrantReplaceRequest(grants);
    }

    private List<ConsoleCatalogResourceEntity> resources(List<PermissionGrantItemRequest> items) {
        List<String> ids = items.stream().map(PermissionGrantItemRequest::resourceId).toList();
        String realm = currentSecurityRealm();
        List<ConsoleCatalogResourceEntity> found = ids.isEmpty()
                ? List.of()
                : resourceDao.selectList(Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .in(ConsoleCatalogResourceEntity::getResourceId, ids)
                        .eq(ConsoleCatalogResourceEntity::getSecurityRealm, realm));
        if (found.size() != ids.size()) {
            invalid("Unknown permission resource");
        }
        Map<String, ConsoleCatalogResourceEntity> byId = found.stream()
                .collect(Collectors.toMap(
                        ConsoleCatalogResourceEntity::getResourceId,
                        value -> value));
        List<ConsoleCatalogResourceEntity> ordered = ids.stream()
                .map(byId::get)
                .toList();
        for (ConsoleCatalogResourceEntity resource : ordered) {
            if (!realm.equals(resource.getSecurityRealm())) {
                invalid("Unknown permission resource");
            }
            if (!isGrantable(resource)) {
                invalid("Resource cannot be granted: " + resource.getResourceKey());
            }
        }
        return ordered;
    }

    private void validateParents(List<ConsoleCatalogResourceEntity> resources) {
        String realm = currentSecurityRealm();
        Set<String> selectedIds = resources.stream()
                .map(ConsoleCatalogResourceEntity::getResourceId)
                .collect(Collectors.toSet());
        Set<String> parentIds = resources.stream()
                .map(ConsoleCatalogResourceEntity::getParentResourceId)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());
        if (parentIds.isEmpty()) {
            return;
        }
        Map<String, ConsoleCatalogResourceEntity> parents = resourceDao.selectList(
                        Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                                .in(ConsoleCatalogResourceEntity::getResourceId, parentIds)
                                .eq(ConsoleCatalogResourceEntity::getSecurityRealm, realm))
                .stream()
                .collect(Collectors.toMap(ConsoleCatalogResourceEntity::getResourceId, value -> value));
        for (ConsoleCatalogResourceEntity resource : resources) {
            String parentId = resource.getParentResourceId();
            if (parentId == null || parentId.isBlank()) {
                continue;
            }
            ConsoleCatalogResourceEntity parent = parents.get(parentId);
            if (parent == null || !BasicStatus.ENABLED.name().equals(parent.getStatus())) {
                invalid("Unknown permission resource parent");
            }
            if (!"MODULE".equals(parent.getResourceType()) && !selectedIds.contains(parentId)) {
                invalid("Permission resource parent must also be granted: "
                        + resource.getResourceKey());
            }
            if ("MENU".equals(resource.getResourceType())
                    && !Set.of("MODULE", "MENU").contains(parent.getResourceType())) {
                invalid("Menu parent must be a module or menu");
            }
            if ("PAGE".equals(resource.getResourceType())
                    && !Set.of("MODULE", "PAGE").contains(parent.getResourceType())) {
                invalid("Page parent must be a module or page");
            }
            if (Set.of("ACTION", "DATASOURCE").contains(resource.getResourceType())
                    && !"PAGE".equals(parent.getResourceType())) {
                invalid("Action and datasource parent must be a page");
            }
        }
    }

    private String normalizeConstraint(
            String value,
            ConsoleCatalogResourceEntity resource
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!CatalogResourceType.DATASOURCE.name().equals(resource.getResourceType())) {
            invalid("Only datasource grants may contain constraints");
        }
        if (value.length() > 10000) {
            invalid("Datasource constraint is too long");
        }
        return value;
    }

    private boolean isGrantable(ConsoleCatalogResourceEntity resource) {
        return resource != null
                && !CatalogResourceType.MODULE.name().equals(resource.getResourceType())
                && BasicStatus.ENABLED.name().equals(resource.getStatus());
    }

    private void validateSubject(
            PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request
    ) {
        Checks.notNull(subjectType, "subjectType");
        Checks.notBlank(subjectId, "subjectId");
        if (request == null) {
            return;
        }
        Set<String> resourceIds = new HashSet<>();
        for (PermissionGrantItemRequest item : request.grants()) {
            if (item == null || item.resourceId() == null || item.resourceId().isBlank()
                    || !resourceIds.add(item.resourceId())) {
                invalid("Grant resourceId must be non-blank and unique");
            }
        }
    }

    private static LambdaQueryWrapper<PermissionGrantEntity> grantScopeQuery(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership
    ) {
        return ConsoleOwnershipScope.apply(Wrappers.<PermissionGrantEntity>lambdaQuery()
                .eq(PermissionGrantEntity::getSubjectType, subjectType.name())
                .eq(PermissionGrantEntity::getSubjectId, subjectId), ownership);
    }

    private static String currentSecurityRealm() {
        String realm = TLC.securityRealm();
        if (realm == null || realm.isBlank()) {
            return "TENANT";
        }
        return realm;
    }

    private static void invalid(String message) {
        throw NexusException.build(NexusStatusCode.INVALID_PARAMETER.fullCode(), message);
    }
}

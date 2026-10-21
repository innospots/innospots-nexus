package com.innospots.nexus.console.permission.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.transaction.Transactional;

import com.innospots.nexus.base.exception.NexusException;
import com.innospots.nexus.console.support.ConsoleExceptions;
import com.innospots.nexus.base.status.NexusStatusCode;
import com.innospots.nexus.base.util.Checks;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantItemRequest;
import com.innospots.nexus.console.permission.domain.request.PermissionGrantReplaceRequest;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 管理角色和组织单元授权，并以全量替换方式保存授权结果。
 *
 * @author Smars
 * @date 2026/09/13
 */
public class PermissionGrantService {

    private final PermissionGrantDao grantDao;
    private final ConsoleCatalogService catalogService;
    private final GrantSubjectAccess grantSubjectAccess;

    /**
     * 创建授权服务。
     */
    public PermissionGrantService(
            PermissionGrantDao grantDao,
            ConsoleCatalogService catalogService,
            GrantSubjectAccess grantSubjectAccess
    ) {
        this.grantDao = Checks.notNull(grantDao, "grantDao");
        this.catalogService = Checks.notNull(catalogService, "catalogService");
        this.grantSubjectAccess = Checks.notNull(grantSubjectAccess, "grantSubjectAccess");
    }

    /**
     * 在一个事务中全量替换指定角色或组织单元的授权。
     *
     * <p>方法先完成主体与 catalog 授权项校验，再删除旧授权并写入新授权，
     * 从而避免部分校验失败时留下不完整的授权集合。</p>
     *
     * @param subjectType 授权主体类型
     * @param subjectId 角色或组织单元 ID
     * @param request 主体最终应拥有的完整授权集合
     */
    /**
     * 全量替换授权；归属由 {@link ConsoleOwnership} 表达，不额外传入租户/工作区 ID。
     */
    @Transactional
    public void replace(
            PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(ownership, "ownership");
        Checks.notNull(request, "request");
        validateSubject(subjectType, subjectId, request);
        grantSubjectAccess.ensureSubjectInScope(subjectType, subjectId, ownership);
        replaceGrants(subjectType, subjectId, request, ownership);
    }

    @Transactional
    public void replace(
            PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Checks.notNull(ownership, "ownership");
        Checks.notNull(request, "request");
        validateSubject(subjectType, subjectId, request);
        grantSubjectAccess.ensureSubjectInScope(subjectType, subjectId, ownership, tenantId, workspaceId);
        replaceGrants(subjectType, subjectId, request, ownership);
    }

    private void replaceGrants(
            PermissionSubjectType subjectType,
            String subjectId,
            PermissionGrantReplaceRequest request,
            ConsoleOwnership ownership
    ) {
        // 全量替换保证撤销的资源不会残留，同时事务保证删除和新增一起提交。
        grantDao.delete(grantScopeQuery(subjectType, subjectId, ownership));
        for (PermissionGrantItemRequest item : request.grants()) {
            PermissionGrantEntity grant = new PermissionGrantEntity();
            ConsoleOwnershipScope.stamp(grant, ownership);
            grant.setSubjectType(subjectType.name());
            grant.setSubjectId(subjectId);
            grant.setGrantKey(item.resourceKey());
            grant.setConstraintDefinition(normalizeConstraint(item.constraintDefinition()));
            grantDao.insert(grant);
        }
    }

    /**
     * 查询当前归属下的授权集合；读取仅用于回显设置，不校验主体可见性。
     *
     * <p>授权行查询本身受 {@link ConsoleOwnership} 隔离，写入时仍由
     * {@link GrantSubjectAccess} 校验主体归属。</p>
     */
    public PermissionGrantReplaceRequest list(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership
    ) {
        Checks.notNull(ownership, "ownership");
        validateSubject(subjectType, subjectId, null);
        return listGrants(subjectType, subjectId, ownership);
    }

    /**
     * 查询指定角色或组织单元在当前归属下的完整授权集合。
     *
     * <p>读取不要求主体仍在会话可见范围内，避免角色删除或归属变化后设置页无法回显。
     * 实际授权行仍按归属过滤。</p>
     *
     * @param subjectType 授权主体类型
     * @param subjectId 角色或组织单元 ID
     * @return 当前授权及 datasource 附加查询条件
     */
    public PermissionGrantReplaceRequest list(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership,
            String tenantId,
            String workspaceId
    ) {
        Checks.notNull(ownership, "ownership");
        validateSubject(subjectType, subjectId, null);
        return listGrants(subjectType, subjectId, ownership);
    }

    private PermissionGrantReplaceRequest listGrants(
            PermissionSubjectType subjectType,
            String subjectId,
            ConsoleOwnership ownership
    ) {
        List<PermissionGrantItemRequest> grants = grantDao.selectList(grantScopeQuery(subjectType, subjectId, ownership))
                .stream()
                .map(grant -> new PermissionGrantItemRequest(
                        grant.getGrantKey(), grant.getConstraintDefinition()))
                .toList();
        return new PermissionGrantReplaceRequest(grants);
    }

    private String normalizeConstraint(String value) {
        if (value != null && !value.isBlank()) {
            invalid("Constraint grants are not supported in catalog v2");
        }
        return null;
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
        Set<String> grantKeys = catalogService.grantResourceKeys();
        Set<String> requestKeys = new HashSet<>();
        for (PermissionGrantItemRequest item : request.grants()) {
            if (item == null || item.resourceKey() == null || item.resourceKey().isBlank()
                    || item.resourceKey().length() > 256
                    || !requestKeys.add(item.resourceKey())) {
                invalid("Grant resourceKey must be non-blank, unique, and at most 256 characters");
            }
            if (!grantKeys.contains(item.resourceKey())) {
                invalid("Unknown permission resource: " + item.resourceKey());
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

    private static void invalid(String message) {
        throw ConsoleExceptions.build(NexusStatusCode.INVALID_PARAMETER.fullCode(), message);
    }
}

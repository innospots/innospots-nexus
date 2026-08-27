package com.innospots.nexus.console.permission.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.dao.PermissionResourceDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.entity.PermissionResourceEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionResourceType;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;

/** 按当前主体构建菜单、页面、action 和 datasource 的可见资源视图。 */
public final class PermissionVisibilityService {

    private final PermissionResourceDao resourceDao;
    private final PermissionGrantDao grantDao;

    /** 创建权限资源可见性服务。 */
    public PermissionVisibilityService(
            PermissionResourceDao resourceDao,
            PermissionGrantDao grantDao
    ) {
        this.resourceDao = resourceDao;
        this.grantDao = grantDao;
    }

    /**
     * 返回主体可见的启用资源。
     *
     * <p>角色和组织单元授权按集合并集处理；子资源必须依赖可见的页面或菜单父节点，MODULE 仅作为
     * 分组节点，不要求单独授权。</p>
     *
     * @param workspaceId 当前 Workspace ID
     * @param subject 当前授权主体
     * @return 按资源目录顺序返回的可见资源；参数无效时返回空集合
     */
    public List<PermissionResourceEntity> visible(
            String workspaceId,
            AuthorizationSubject subject
    ) {
        if (workspaceId == null || subject == null) {
            return List.of();
        }
        List<PermissionResourceEntity> resources = resourceDao.selectList(
                Wrappers.<PermissionResourceEntity>lambdaQuery()
                        .eq(PermissionResourceEntity::getWorkspaceId, workspaceId)
                        .eq(PermissionResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        if (subject.administrator()) {
            return List.copyOf(resources);
        }
        Set<String> grantedIds = grantedResourceIds(workspaceId, subject);
        Map<String, PermissionResourceEntity> byId = resources.stream()
                .filter(value -> value.getResourceId() != null)
                .collect(Collectors.toMap(
                        PermissionResourceEntity::getResourceId,
                        Function.identity()));
        Set<String> visibleIds = new HashSet<>();
        int previousSize;
        do {
            previousSize = visibleIds.size();
            // 数据库返回顺序不可靠，循环直到父子可见集合不再变化。
            for (PermissionResourceEntity resource : resources) {
                if (!PermissionResourceType.MODULE.name().equals(resource.getResourceType())
                        && grantedIds.contains(resource.getResourceId())
                        && parentVisible(resource, byId, visibleIds)) {
                    visibleIds.add(resource.getResourceId());
                }
            }
        } while (visibleIds.size() != previousSize);
        for (PermissionResourceEntity resource : resources) {
            if (PermissionResourceType.MODULE.name().equals(resource.getResourceType())
                    && resources.stream().anyMatch(child ->
                    resource.getResourceId().equals(child.getParentResourceId())
                            && visibleIds.contains(child.getResourceId()))) {
                visibleIds.add(resource.getResourceId());
            }
        }
        List<PermissionResourceEntity> visible = new ArrayList<>();
        for (PermissionResourceEntity resource : resources) {
            if (visibleIds.contains(resource.getResourceId())) {
                visible.add(resource);
            }
        }
        return List.copyOf(visible);
    }

    private Set<String> grantedResourceIds(String workspaceId, AuthorizationSubject subject) {
        List<PermissionGrantEntity> grants = new ArrayList<>();
        if (!subject.roleIds().isEmpty()) {
            grants.addAll(grantDao.selectList(Wrappers.<PermissionGrantEntity>lambdaQuery()
                    .eq(PermissionGrantEntity::getWorkspaceId, workspaceId)
                    .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                    .in(PermissionGrantEntity::getSubjectId, subject.roleIds())));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            grants.addAll(grantDao.selectList(Wrappers.<PermissionGrantEntity>lambdaQuery()
                    .eq(PermissionGrantEntity::getWorkspaceId, workspaceId)
                    .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                    .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds())));
        }
        return grants.stream()
                .map(PermissionGrantEntity::getResourceId)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());
    }

    private boolean parentVisible(
            PermissionResourceEntity resource,
            Map<String, PermissionResourceEntity> byId,
            Set<String> visibleIds
    ) {
        String parentId = resource.getParentResourceId();
        if (parentId == null || parentId.isBlank()) {
            return true;
        }
        PermissionResourceEntity parent = byId.get(parentId);
        return parent != null
                && (PermissionResourceType.MODULE.name().equals(parent.getResourceType())
                || visibleIds.contains(parentId));
    }
}

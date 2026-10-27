package com.innospots.nexus.console.permission.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.catalog.domain.model.CatalogPermissionGroupDeclarations;
import com.innospots.nexus.console.menu.dao.MenuDao;
import com.innospots.nexus.console.menu.domain.entity.MenuEntity;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.domain.entity.PermissionGrantEntity;
import com.innospots.nexus.console.permission.domain.enums.PermissionSubjectType;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 按当前主体构建可见资源与菜单视图。
 *
 * @author Smars
 * @date 2026/09/13
 */
public final class PermissionVisibilityService {

    private final ConsoleCatalogResourceDao resourceDao;
    private final MenuDao menuDao;
    private final PermissionGrantDao grantDao;

    /**
     * 创建权限资源可见性服务。
     */
    public PermissionVisibilityService(
            ConsoleCatalogResourceDao resourceDao,
            MenuDao menuDao,
            PermissionGrantDao grantDao
    ) {
        this.resourceDao = resourceDao;
        this.menuDao = menuDao;
        this.grantDao = grantDao;
    }

    /**
     * 返回主体可见的启用资源。
     *
     * <p>角色和组织单元授权按集合并集处理；子资源必须依赖可见的页面或菜单父节点，MODULE 仅作为
     * 分组节点，不要求单独授权。</p>
     *
     * @param ownership 当前控制台归属
     * @param subject 当前授权主体
     * @return 按资源目录顺序返回的可见资源；参数无效时返回空集合
     */
    public List<ConsoleCatalogResourceEntity> visible(
            ConsoleOwnership ownership,
            AuthorizationSubject subject
    ) {
        if (ownership == null || subject == null) {
            return List.of();
        }
        List<ConsoleCatalogResourceEntity> resources = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name()));
        if (subject.administrator()) {
            return List.copyOf(resources);
        }
        Set<String> grantedKeys = grantedGrantKeys(ownership, subject);
        Map<String, ConsoleCatalogResourceEntity> byId = resources.stream()
                .filter(value -> value.getResourceId() != null)
                .collect(Collectors.toMap(
                        ConsoleCatalogResourceEntity::getResourceId,
                        Function.identity()));
        Set<String> visibleIds = new HashSet<>();
        int previousSize;
        do {
            previousSize = visibleIds.size();
            // 数据库返回顺序不可靠，循环直到父子可见集合不再变化。
            for (ConsoleCatalogResourceEntity resource : resources) {
                if (isGrantablePageResource(resource)
                        && grantKeyGranted(resource, grantedKeys)
                        && parentVisible(resource, byId, visibleIds)) {
                    visibleIds.add(resource.getResourceId());
                }
            }
        } while (visibleIds.size() != previousSize);
        for (ConsoleCatalogResourceEntity resource : resources) {
            if (CatalogResourceType.MODULE.name().equals(resource.getResourceType())
                    && resources.stream().anyMatch(child ->
                    resource.getResourceId().equals(child.getParentResourceId())
                            && visibleIds.contains(child.getResourceId()))) {
                visibleIds.add(resource.getResourceId());
            }
        }
        List<ConsoleCatalogResourceEntity> visible = new ArrayList<>();
        for (ConsoleCatalogResourceEntity resource : resources) {
            if (visibleIds.contains(resource.getResourceId())) {
                visible.add(resource);
            }
        }
        return List.copyOf(visible);
    }

    /**
     * 返回当前归属下的全部启用且可见菜单，不执行权限过滤。
     *
     * @param ownership 当前控制台归属
     * @return 按持久化顺序返回的菜单记录；参数无效时返回空集合
     */
    public List<MenuEntity> allMenus(ConsoleOwnership ownership) {
        if (ownership == null) {
            return List.of();
        }
        return List.copyOf(menuDao.selectList(
                ConsoleOwnershipScope.apply(Wrappers.<MenuEntity>lambdaQuery()
                        .eq(MenuEntity::getStatus, BasicStatus.ENABLED.name())
                        .eq(MenuEntity::getVisible, Boolean.TRUE), ownership)));
    }

    /**
     * 返回当前主体可见的菜单记录。
     *
     * <p>菜单事实源为 {@link MenuEntity}；catalog 资源只用于解释菜单对应的浏览分组授权键。
     * 管理员返回全部启用菜单，普通主体保留已授权菜单及其父目录。</p>
     *
     * @param ownership 当前控制台归属
     * @param subject 当前授权主体
     * @return 按持久化顺序返回的可见菜单；参数无效时返回空集合
     */
    public List<MenuEntity> visibleMenus(
            ConsoleOwnership ownership,
            AuthorizationSubject subject
    ) {
        if (ownership == null || subject == null) {
            return List.of();
        }
        List<MenuEntity> menus = allMenus(ownership);
        if (menus.isEmpty() || subject.administrator()) {
            return menus;
        }
        Set<String> grantedKeys = grantedGrantKeys(ownership, subject);
        if (grantedKeys.isEmpty()) {
            return List.of();
        }
        Map<String, List<ConsoleCatalogResourceEntity>> resourcesByMenuKey =
                permissionResourcesByMenuKey(ownership.securityRealm());
        Set<String> visibleIds = menus.stream()
                .filter(menu -> hasText(menu.getMenuId()))
                .filter(menu -> menuGranted(menu, grantedKeys, resourcesByMenuKey))
                .map(MenuEntity::getMenuId)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> knownIds = menus.stream()
                .map(MenuEntity::getMenuId)
                .filter(PermissionVisibilityService::hasText)
                .collect(Collectors.toSet());
        int previousSize;
        do {
            previousSize = visibleIds.size();
            for (MenuEntity menu : menus) {
                if (hasText(menu.getParentId())
                        && knownIds.contains(menu.getParentId())
                        && visibleIds.contains(menu.getMenuId())) {
                    visibleIds.add(menu.getParentId());
                }
            }
        } while (visibleIds.size() != previousSize);
        List<MenuEntity> visible = new ArrayList<>();
        for (MenuEntity menu : menus) {
            if (visibleIds.contains(menu.getMenuId())) {
                visible.add(menu);
            }
        }
        return List.copyOf(visible);
    }

    private Set<String> grantedGrantKeys(ConsoleOwnership ownership, AuthorizationSubject subject) {
        List<PermissionGrantEntity> grants = new ArrayList<>();
        if (!subject.roleIds().isEmpty()) {
            grants.addAll(grantDao.selectList(scopeGrantQuery(ownership)
                    .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ROLE.name())
                    .in(PermissionGrantEntity::getSubjectId, subject.roleIds())));
        }
        if (!subject.orgUnitIds().isEmpty()) {
            grants.addAll(grantDao.selectList(scopeGrantQuery(ownership)
                    .eq(PermissionGrantEntity::getSubjectType, PermissionSubjectType.ORG_UNIT.name())
                    .in(PermissionGrantEntity::getSubjectId, subject.orgUnitIds())));
        }
        return grants.stream()
                .map(PermissionGrantEntity::getGrantKey)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toSet());
    }

    private Map<String, List<ConsoleCatalogResourceEntity>> permissionResourcesByMenuKey(
            String securityRealm
    ) {
        List<ConsoleCatalogResourceEntity> resources = resourceDao.selectList(
                Wrappers.<ConsoleCatalogResourceEntity>lambdaQuery()
                        .eq(ConsoleCatalogResourceEntity::getStatus, BasicStatus.ENABLED.name())
                        .eq(ConsoleCatalogResourceEntity::getSecurityRealm, securityRealm));
        Map<String, List<ConsoleCatalogResourceEntity>> byMenuKey = new LinkedHashMap<>();
        for (ConsoleCatalogResourceEntity resource : resources) {
            addResourceKey(byMenuKey, resource.getEntryPageKey(), resource);
            addResourceKey(byMenuKey, resource.getResourceKey(), resource);
            addResourceKey(byMenuKey, catalogMenuKey(resource.getResourceKey()), resource);
        }
        return byMenuKey;
    }

    private static void addResourceKey(
            Map<String, List<ConsoleCatalogResourceEntity>> byMenuKey,
            String menuKey,
            ConsoleCatalogResourceEntity resource
    ) {
        if (hasText(menuKey)) {
            byMenuKey.computeIfAbsent(menuKey, ignored -> new ArrayList<>()).add(resource);
        }
    }

    private static boolean menuGranted(
            MenuEntity menu,
            Set<String> grantedKeys,
            Map<String, List<ConsoleCatalogResourceEntity>> resourcesByMenuKey
    ) {
        if (grantedKeys.contains(menu.getMenuKey())) {
            return true;
        }
        for (ConsoleCatalogResourceEntity resource
                : resourcesByMenuKey.getOrDefault(menu.getMenuKey(), List.of())) {
            for (String viewKey : CatalogPermissionGroupDeclarations.viewGrantKeys(resource)) {
                if (grantedKeys.contains(viewKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String catalogMenuKey(String resourceKey) {
        if (resourceKey == null || !resourceKey.startsWith("menu:")) {
            return resourceKey;
        }
        String suffix = resourceKey.substring("menu:".length());
        int separator = suffix.indexOf('.');
        if (separator < 0 || separator == suffix.length() - 1) {
            return suffix;
        }
        return suffix.substring(separator + 1);
    }

    private static boolean grantKeyGranted(
            ConsoleCatalogResourceEntity resource,
            Set<String> grantedKeys
    ) {
        for (String viewKey : CatalogPermissionGroupDeclarations.viewGrantKeys(resource)) {
            if (grantedKeys.contains(viewKey)) {
                return true;
            }
        }
        return false;
    }

    private static LambdaQueryWrapper<PermissionGrantEntity> scopeGrantQuery(ConsoleOwnership ownership) {
        return ConsoleOwnershipScope.apply(Wrappers.<PermissionGrantEntity>lambdaQuery(), ownership);
    }

    private boolean parentVisible(
            ConsoleCatalogResourceEntity resource,
            Map<String, ConsoleCatalogResourceEntity> byId,
            Set<String> visibleIds
    ) {
        String parentId = resource.getParentResourceId();
        if (parentId == null || parentId.isBlank()) {
            return true;
        }
        ConsoleCatalogResourceEntity parent = byId.get(parentId);
        return parent != null
                && (CatalogResourceType.MODULE.name().equals(parent.getResourceType())
                || visibleIds.contains(parentId));
    }

    private static boolean isGrantablePageResource(ConsoleCatalogResourceEntity resource) {
        if (CatalogResourceType.MENU.name().equals(resource.getResourceType())) {
            return true;
        }
        return CatalogResourceType.MODULE.name().equals(resource.getResourceType())
                && resource.getEntryPageKey() != null
                && !resource.getEntryPageKey().isBlank();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

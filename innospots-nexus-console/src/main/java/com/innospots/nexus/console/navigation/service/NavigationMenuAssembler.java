package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.catalog.domain.entity.ConsoleCatalogResourceEntity;
import com.innospots.nexus.console.catalog.domain.enums.CatalogResourceType;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 从持久化权限目录组装当前用户可见的导航菜单树。
 */
public final class NavigationMenuAssembler {

    private final PermissionVisibilityService visibilityService;

    public NavigationMenuAssembler(PermissionVisibilityService visibilityService) {
        if (visibilityService == null) {
            throw new IllegalArgumentException("visibilityService is required");
        }
        this.visibilityService = visibilityService;
    }

    /**
     * 按工作区归属返回可见 MENU 树（租户/工作区会话）。
     */
    public List<NavigationMenuVo> navigationMenus(String workspaceId, AuthorizationSubject subject) {
        if (workspaceId == null || workspaceId.isBlank() || subject == null) {
            return List.of();
        }
        String realm = TLC.securityRealm();
        if (realm == null || realm.isBlank()) {
            realm = SecurityRealm.TENANT.name();
        }
        return navigationMenus(ConsoleOwnershipScope.workspaceOwnership(workspaceId, realm), subject);
    }

    /**
     * 按显式归属返回可见 MENU 树（如 PLATFORM 会话）。
     */
    public List<NavigationMenuVo> navigationMenus(ConsoleOwnership ownership, AuthorizationSubject subject) {
        if (ownership == null || subject == null) {
            return List.of();
        }
        return buildMenus(visibilityService.visible(ownership, subject));
    }

    private List<NavigationMenuVo> buildMenus(List<ConsoleCatalogResourceEntity> visible) {
        Map<String, ConsoleCatalogResourceEntity> pagesByPageKey = visible.stream()
                .filter(resource -> CatalogResourceType.PAGE.name().equals(resource.getResourceType()))
                .filter(resource -> resource.getPageKey() != null && !resource.getPageKey().isBlank())
                .collect(Collectors.toMap(
                        ConsoleCatalogResourceEntity::getPageKey,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));
        Set<String> visibleMenuIds = visible.stream()
                .filter(resource -> CatalogResourceType.MENU.name().equals(resource.getResourceType()))
                .map(ConsoleCatalogResourceEntity::getResourceId)
                .collect(Collectors.toCollection(HashSet::new));
        Map<String, List<ConsoleCatalogResourceEntity>> menusByParent = new LinkedHashMap<>();
        List<ConsoleCatalogResourceEntity> roots = new ArrayList<>();
        for (ConsoleCatalogResourceEntity resource : visible) {
            if (!CatalogResourceType.MENU.name().equals(resource.getResourceType())) {
                continue;
            }
            String parentId = resource.getParentResourceId();
            if (parentId == null
                    || parentId.isBlank()
                    || !visibleMenuIds.contains(parentId)) {
                roots.add(resource);
            } else {
                menusByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(resource);
            }
        }
        roots.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<NavigationMenuVo> menus = new ArrayList<>();
        for (ConsoleCatalogResourceEntity root : roots) {
            menus.add(toMenu(root, menusByParent, pagesByPageKey));
        }
        return List.copyOf(menus);
    }

    private static NavigationMenuVo toMenu(
            ConsoleCatalogResourceEntity menu,
            Map<String, List<ConsoleCatalogResourceEntity>> menusByParent,
            Map<String, ConsoleCatalogResourceEntity> pagesByPageKey
    ) {
        List<ConsoleCatalogResourceEntity> children = menusByParent.getOrDefault(
                menu.getResourceId(), List.of());
        children = new ArrayList<>(children);
        children.sort(Comparator.comparing(ConsoleCatalogResourceEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<NavigationMenuVo> childMenus = new ArrayList<>();
        for (ConsoleCatalogResourceEntity child : children) {
            childMenus.add(toMenu(child, menusByParent, pagesByPageKey));
        }
        return new NavigationMenuVo(
                menuKey(menu.getResourceKey()),
                menu.getDisplayName(),
                routePath(menu, pagesByPageKey),
                null,
                null,
                null,
                MenuOpenMode.INTERNAL,
                menu.getResourceId(),
                menu.getOwnerPluginId(),
                menu.getModuleKey(),
                menu.getPageKey(),
                childMenus);
    }

    private static String routePath(
            ConsoleCatalogResourceEntity menu,
            Map<String, ConsoleCatalogResourceEntity> pagesByPageKey
    ) {
        if (menu.getPageKey() == null || menu.getPageKey().isBlank()) {
            return null;
        }
        ConsoleCatalogResourceEntity page = pagesByPageKey.get(menu.getPageKey());
        if (page == null) {
            return null;
        }
        return page.getRoutePath();
    }

    private static String menuKey(String resourceKey) {
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
}

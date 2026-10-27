package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.innospots.nexus.base.thread.TLC;
import com.innospots.nexus.console.auth.domain.enums.SecurityRealm;
import com.innospots.nexus.console.menu.domain.entity.MenuEntity;
import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.scope.ConsoleOwnershipScope;

/**
 * 从持久化菜单记录组装当前用户可见的导航菜单树。
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
        return buildMenus(visibilityService.visibleMenus(ownership, subject));
    }

    /**
     * 返回当前归属下的全部启用菜单，不执行权限过滤。
     */
    public List<NavigationMenuVo> allNavigationMenus(ConsoleOwnership ownership) {
        if (ownership == null) {
            return List.of();
        }
        return buildMenus(visibilityService.allMenus(ownership));
    }

    private List<NavigationMenuVo> buildMenus(List<MenuEntity> visible) {
        Set<String> visibleMenuIds = visible.stream()
                .map(MenuEntity::getMenuId)
                .filter(NavigationMenuAssembler::hasText)
                .collect(Collectors.toCollection(HashSet::new));
        Map<String, List<MenuEntity>> menusByParent = new LinkedHashMap<>();
        List<MenuEntity> roots = new ArrayList<>();
        for (MenuEntity menu : visible) {
            String parentId = menu.getParentId();
            if (parentId == null
                    || parentId.isBlank()
                    || !visibleMenuIds.contains(parentId)) {
                roots.add(menu);
            } else {
                menusByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(menu);
            }
        }
        roots.sort(Comparator.comparing(MenuEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<NavigationMenuVo> menus = new ArrayList<>();
        for (MenuEntity root : roots) {
            menus.add(toMenu(root, menusByParent));
        }
        return List.copyOf(menus);
    }

    private static NavigationMenuVo toMenu(
            MenuEntity menu,
            Map<String, List<MenuEntity>> menusByParent
    ) {
        List<MenuEntity> children = menusByParent.getOrDefault(
                menu.getMenuId(), List.of());
        children = new ArrayList<>(children);
        children.sort(Comparator.comparing(MenuEntity::getSortOrder,
                Comparator.nullsLast(Integer::compareTo)));
        List<NavigationMenuVo> childMenus = new ArrayList<>();
        for (MenuEntity child : children) {
            childMenus.add(toMenu(child, menusByParent));
        }
        return new NavigationMenuVo(
                menu.getMenuKey(),
                menu.getMenuName(),
                menu.getRoutePath(),
                menu.getComponentKey(),
                menu.getExternalUrl(),
                menu.getIcon(),
                openMode(menu.getOpenMode()),
                null,
                null,
                null,
                null,
                childMenus);
    }

    private static MenuOpenMode openMode(String value) {
        if (value == null || value.isBlank()) {
            return MenuOpenMode.INTERNAL;
        }
        try {
            return MenuOpenMode.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return MenuOpenMode.INTERNAL;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

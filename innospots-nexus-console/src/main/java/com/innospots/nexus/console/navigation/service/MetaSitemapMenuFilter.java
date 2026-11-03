package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.scope.ConsoleOwnership;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

/**
 * 按 meta-sitemap 节点上的 catalog 浏览授权、{@code roles} 与 {@code hidden} 裁剪默认菜单。
 */
public final class MetaSitemapMenuFilter {

    private final PermissionVisibilityService visibilityService;

    public MetaSitemapMenuFilter(PermissionVisibilityService visibilityService) {
        if (visibilityService == null) {
            throw new IllegalArgumentException("visibilityService is required");
        }
        this.visibilityService = visibilityService;
    }

    /**
     * 剔除 {@code hidden: true} 节点；不执行 catalog / 角色裁剪。
     */
    public List<SitemapMenuItem> withoutHiddenMenus(List<SitemapMenuItem> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem item : menus) {
            SitemapMenuItem copy = withoutHiddenNode(item);
            if (copy != null) {
                visible.add(copy);
            }
        }
        return List.copyOf(visible);
    }

    /**
     * 返回当前主体在指定归属下可见的菜单子树。
     */
    public List<SitemapMenuItem> filterVisibleMenus(
            List<SitemapMenuItem> menus,
            ConsoleOwnership ownership,
            AuthorizationSubject subject
    ) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        Set<String> grantedKeys = visibilityService.grantedGrantKeys(ownership, subject);
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem item : menus) {
            SitemapMenuItem copy = filterNode(item, subject, grantedKeys);
            if (copy != null) {
                visible.add(copy);
            }
        }
        return List.copyOf(visible);
    }

    private static SitemapMenuItem filterNode(
            SitemapMenuItem item,
            AuthorizationSubject subject,
            Set<String> grantedKeys
    ) {
        if (item == null || Boolean.TRUE.equals(item.getHidden())) {
            return null;
        }
        if (!catalogGrantAllowed(item, subject, grantedKeys)) {
            return null;
        }
        if (!roleAllowed(item.getRoles(), subject)) {
            return null;
        }
        List<SitemapMenuItem> children = filterVisibleChildren(item.getChildren(), subject, grantedKeys);
        if ("group".equals(item.getType()) && children.isEmpty()) {
            return null;
        }
        SitemapMenuItem copy = shallowCopy(item);
        copy.setChildren(children);
        return copy;
    }

    private static SitemapMenuItem withoutHiddenNode(SitemapMenuItem item) {
        if (item == null || Boolean.TRUE.equals(item.getHidden())) {
            return null;
        }
        List<SitemapMenuItem> children = withoutHiddenChildren(item.getChildren());
        if ("group".equals(item.getType()) && children.isEmpty()) {
            return null;
        }
        SitemapMenuItem copy = shallowCopy(item);
        copy.setChildren(children);
        return copy;
    }

    private static List<SitemapMenuItem> withoutHiddenChildren(List<SitemapMenuItem> children) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem child : children) {
            SitemapMenuItem filtered = withoutHiddenNode(child);
            if (filtered != null) {
                visible.add(filtered);
            }
        }
        return List.copyOf(visible);
    }

    private static List<SitemapMenuItem> filterVisibleChildren(
            List<SitemapMenuItem> children,
            AuthorizationSubject subject,
            Set<String> grantedKeys
    ) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem child : children) {
            SitemapMenuItem filtered = filterNode(child, subject, grantedKeys);
            if (filtered != null) {
                visible.add(filtered);
            }
        }
        return List.copyOf(visible);
    }

    private static boolean catalogGrantAllowed(
            SitemapMenuItem item,
            AuthorizationSubject subject,
            Set<String> grantedKeys
    ) {
        if (!item.hasCatalogGrantBinding()) {
            return true;
        }
        if (subject != null && subject.administrator()) {
            return true;
        }
        if (grantedKeys.contains("*")) {
            return true;
        }
        String viewGrantKey = item.viewGrantKey();
        return viewGrantKey != null && grantedKeys.contains(viewGrantKey);
    }

    private static boolean roleAllowed(List<String> roles, AuthorizationSubject subject) {
        if (roles == null || roles.isEmpty()) {
            return true;
        }
        if (subject != null && subject.administrator()) {
            return true;
        }
        Set<String> subjectRoles = subject == null ? Set.of() : subject.roleIds();
        Set<String> required = new HashSet<>(roles);
        for (String role : subjectRoles) {
            if (required.contains(role)) {
                return true;
            }
        }
        return false;
    }

    private static SitemapMenuItem shallowCopy(SitemapMenuItem source) {
        SitemapMenuItem copy = new SitemapMenuItem();
        copy.setId(source.getId());
        copy.setType(source.getType());
        copy.setPageId(source.getPageId());
        copy.setTitle(source.getTitle());
        copy.setIcon(source.getIcon());
        copy.setLayout(source.getLayout());
        copy.setOrder(source.getOrder());
        copy.setPublicAccess(source.getPublicAccess());
        copy.setHidden(source.getHidden());
        copy.setActiveMenu(source.getActiveMenu());
        copy.setDomainKey(source.getDomainKey());
        copy.setModuleKey(source.getModuleKey());
        copy.setViewGroup(source.getViewGroup());
        copy.setRoles(source.getRoles() == null ? List.of() : List.copyOf(source.getRoles()));
        return copy;
    }
}

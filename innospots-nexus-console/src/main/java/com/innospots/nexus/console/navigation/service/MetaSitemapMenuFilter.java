package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.innospots.nexus.console.permission.authorization.AuthorizationSubject;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

/**
 * 按 meta-sitemap 节点上的 {@code roles} / {@code hidden} 裁剪默认菜单（不做 catalog 权限判定）。
 */
public final class MetaSitemapMenuFilter {

    /**
     * 返回当前主体可见的菜单子树（剔除 {@code hidden} 与角色不匹配项）。
     */
    public List<SitemapMenuItem> filterVisibleMenus(
            List<SitemapMenuItem> menus,
            AuthorizationSubject subject
    ) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem item : menus) {
            SitemapMenuItem copy = filterNode(item, subject);
            if (copy != null) {
                visible.add(copy);
            }
        }
        return List.copyOf(visible);
    }

    private static SitemapMenuItem filterNode(SitemapMenuItem item, AuthorizationSubject subject) {
        if (item == null || Boolean.TRUE.equals(item.getHidden())) {
            return null;
        }
        if (!roleAllowed(item.getRoles(), subject)) {
            return null;
        }
        List<SitemapMenuItem> children = filterVisibleChildren(item.getChildren(), subject);
        if ("group".equals(item.getType()) && children.isEmpty()) {
            return null;
        }
        SitemapMenuItem copy = shallowCopy(item);
        copy.setChildren(children);
        return copy;
    }

    private static List<SitemapMenuItem> filterVisibleChildren(
            List<SitemapMenuItem> children,
            AuthorizationSubject subject
    ) {
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        List<SitemapMenuItem> visible = new ArrayList<>();
        for (SitemapMenuItem child : children) {
            SitemapMenuItem filtered = filterNode(child, subject);
            if (filtered != null) {
                visible.add(filtered);
            }
        }
        return List.copyOf(visible);
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
        copy.setRoles(source.getRoles() == null ? List.of() : List.copyOf(source.getRoles()));
        return copy;
    }
}

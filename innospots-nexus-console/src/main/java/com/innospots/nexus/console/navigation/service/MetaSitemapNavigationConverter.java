package com.innospots.nexus.console.navigation.service;

import java.util.ArrayList;
import java.util.List;

import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.vo.NavigationMenuVo;
import com.innospots.nexus.console.sitemap.domain.model.SitemapMenuItem;

/**
 * 将 meta-sitemap 菜单节点转换为 {@link NavigationMenuVo}（仅导航展示字段）。
 */
public final class MetaSitemapNavigationConverter {

    public List<NavigationMenuVo> toNavigationMenus(List<SitemapMenuItem> menus) {
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }
        List<NavigationMenuVo> result = new ArrayList<>();
        for (SitemapMenuItem menu : menus) {
            NavigationMenuVo converted = toNavigationMenu(menu);
            if (converted != null) {
                result.add(converted);
            }
        }
        return List.copyOf(result);
    }

    private static NavigationMenuVo toNavigationMenu(SitemapMenuItem item) {
        if (item == null || Boolean.TRUE.equals(item.getHidden())) {
            return null;
        }
        if ("group".equals(item.getType())) {
            List<NavigationMenuVo> children = new ArrayList<>();
            for (SitemapMenuItem child : item.getChildren()) {
                NavigationMenuVo converted = toNavigationMenu(child);
                if (converted != null) {
                    children.add(converted);
                }
            }
            if (children.isEmpty()) {
                return null;
            }
            return new NavigationMenuVo(
                    item.getId(),
                    item.getTitle(),
                    null,
                    null,
                    null,
                    item.getIcon(),
                    MenuOpenMode.INTERNAL,
                    null,
                    null,
                    null,
                    null,
                    List.copyOf(children));
        }
        if (!"page".equals(item.getType())) {
            return null;
        }
        return new NavigationMenuVo(
                item.getId(),
                item.getTitle(),
                null,
                null,
                null,
                item.getIcon(),
                MenuOpenMode.INTERNAL,
                null,
                null,
                null,
                item.getPageId(),
                List.of());
    }
}

package com.innospots.nexus.kernel.menu.domain.vo;

import java.util.List;

import com.innospots.nexus.kernel.menu.domain.enums.MenuOpenMode;

/**
 * Read-only navigation node delivered to the management frontend.
 *
 * @param menuKey      stable menu key
 * @param menuName     display name
 * @param routePath    internal navigation path
 * @param componentKey logical frontend component identifier
 * @param externalUrl  external destination
 * @param icon         optional icon identifier
 * @param openMode     browser target mode
 * @param children     nested visible navigation nodes
 */
public record NavigationMenuVo(
        String menuKey,
        String menuName,
        String routePath,
        String componentKey,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        List<NavigationMenuVo> children
) {

    public NavigationMenuVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

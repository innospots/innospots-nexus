package com.innospots.nexus.kernel.menu.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.kernel.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.kernel.menu.domain.enums.MenuType;

/**
 * Management-console menu details and nested children.
 *
 * @param menuId       menu identifier
 * @param parentId     optional parent menu identifier
 * @param menuKey      stable project-unique menu key
 * @param menuName     display name
 * @param menuType     menu node type
 * @param routePath    internal navigation path
 * @param componentKey logical frontend component identifier
 * @param redirectPath optional redirect path
 * @param externalUrl  external destination
 * @param icon         optional icon identifier
 * @param openMode     browser target mode
 * @param visible      whether the node appears in navigation
 * @param status       lifecycle status
 * @param sortOrder    sibling display order
 * @param builtIn      whether the node is system-managed
 * @param createdTime  creation time
 * @param updatedTime  last update time
 * @param children     nested child menus
 */
public record MenuVo(
        String menuId,
        String parentId,
        String menuKey,
        String menuName,
        MenuType menuType,
        String routePath,
        String componentKey,
        String redirectPath,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        Boolean visible,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdTime,
        LocalDateTime updatedTime,
        List<MenuVo> children
) {

    public MenuVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}

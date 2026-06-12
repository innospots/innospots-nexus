package com.innospots.nexus.kernel.menu.domain.request;

import com.innospots.nexus.kernel.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.kernel.menu.domain.enums.MenuType;

/**
 * Mutable profile data for an existing menu node.
 * <p>
 * The stable menu key is deliberately excluded from updates.
 * </p>
 *
 * @param parentId     optional parent menu identifier
 * @param menuName     display name
 * @param menuType     menu node type
 * @param routePath    internal navigation path
 * @param componentKey logical frontend component identifier
 * @param redirectPath optional redirect path
 * @param externalUrl  external destination for link menus
 * @param icon         optional icon identifier
 * @param openMode     browser target mode
 * @param visible      whether the node appears in navigation
 * @param sortOrder    sibling display order
 */
public record MenuUpdateRequest(
        String parentId,
        String menuName,
        MenuType menuType,
        String routePath,
        String componentKey,
        String redirectPath,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        Boolean visible,
        Integer sortOrder
) {
}

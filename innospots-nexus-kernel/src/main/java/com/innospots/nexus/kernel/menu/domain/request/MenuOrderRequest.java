package com.innospots.nexus.kernel.menu.domain.request;

import java.util.List;

/**
 * Ordered sibling menu identifiers under one parent.
 *
 * @param parentId optional parent menu identifier for root menus
 * @param menuIds  menu identifiers in target display order
 */
public record MenuOrderRequest(String parentId, List<String> menuIds) {

    public MenuOrderRequest {
        menuIds = menuIds == null ? List.of() : List.copyOf(menuIds);
    }
}

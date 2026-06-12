package com.innospots.nexus.kernel.menu.domain.request;

import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.kernel.menu.domain.enums.MenuType;

/**
 * Management menu-tree filters bound from query parameters.
 *
 * @param input    fuzzy menu name or key
 * @param menuType optional menu type
 * @param status   optional lifecycle status
 * @param visible  optional navigation visibility
 */
public record MenuTreeRequest(
        @QueryParam("input") String input,
        @QueryParam("menuType") MenuType menuType,
        @QueryParam("status") BasicStatus status,
        @QueryParam("visible") Boolean visible
) {

    /**
     * Creates an unfiltered query for Jakarta REST bean binding.
     */
    public MenuTreeRequest() {
        this(null, null, null, null);
    }
}

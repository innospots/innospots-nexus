package com.innospots.nexus.kernel.group.domain.request;

import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Group tree filters.
 *
 * @param input  fuzzy group name or code
 * @param status optional lifecycle status
 */
public record GroupTreeRequest(
        @QueryParam("input") String input,
        @QueryParam("status") BasicStatus status
) {

    /**
     * Creates an unfiltered query for Jakarta REST bean binding.
     */
    public GroupTreeRequest() {
        this(null, null);
    }
}

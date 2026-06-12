package com.innospots.nexus.kernel.group.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated group member filters.
 *
 * @param input    fuzzy user identity input
 * @param leader   optional leader flag
 * @param pageNo   1-indexed page number
 * @param pageSize page size
 */
public record GroupMemberPageRequest(
        @QueryParam("input") String input,
        @QueryParam("leader") Boolean leader,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public GroupMemberPageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    /**
     * Creates an unfiltered first-page query for Jakarta REST bean binding.
     */
    public GroupMemberPageRequest() {
        this(null, null,
                SimpleQueryRequest.DEFAULT_PAGE_NO,
                SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

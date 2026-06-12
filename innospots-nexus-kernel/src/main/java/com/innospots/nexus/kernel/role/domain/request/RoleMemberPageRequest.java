package com.innospots.nexus.kernel.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated query for users assigned to a role.
 *
 * @param input    fuzzy user name, display name, email, or mobile
 * @param pageNo   1-indexed page number
 * @param pageSize page size
 */
public record RoleMemberPageRequest(
        @QueryParam("input") String input,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public RoleMemberPageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public RoleMemberPageRequest() {
        this(null, SimpleQueryRequest.DEFAULT_PAGE_NO, SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

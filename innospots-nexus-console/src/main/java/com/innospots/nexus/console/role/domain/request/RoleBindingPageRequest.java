package com.innospots.nexus.console.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * Paginated query for subjects bound to a role.
 *
 * @param input        fuzzy subject identifier
 * @param subjectType  optional USER or ORG_UNIT filter
 * @param pageNo       1-indexed page number
 * @param pageSize     page size
 */
public record RoleBindingPageRequest(
        @QueryParam("input") String input,
        @QueryParam("subjectType") RoleBindingSubjectType subjectType,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public RoleBindingPageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public RoleBindingPageRequest() {
        this(null, null, SimpleQueryRequest.DEFAULT_PAGE_NO, SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

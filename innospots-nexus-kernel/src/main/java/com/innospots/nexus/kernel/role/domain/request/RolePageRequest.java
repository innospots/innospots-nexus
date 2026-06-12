package com.innospots.nexus.kernel.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated role query bound from management-console query parameters.
 *
 * @param input    fuzzy role name or code
 * @param status   optional lifecycle status
 * @param builtIn  optional built-in role filter
 * @param pageNo   1-indexed page number
 * @param pageSize page size
 */
public record RolePageRequest(
        @QueryParam("input") String input,
        @QueryParam("status") BasicStatus status,
        @QueryParam("builtIn") Boolean builtIn,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public RolePageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public RolePageRequest() {
        this(null, null, null,
                SimpleQueryRequest.DEFAULT_PAGE_NO,
                SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

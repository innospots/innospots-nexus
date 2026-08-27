package com.innospots.nexus.console.dictionary.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated dictionary type query bound from management-console query parameters.
 *
 * @param input    fuzzy type name or code
 * @param status   optional lifecycle status
 * @param builtIn  optional built-in type filter
 * @param pageNo   1-indexed page number
 * @param pageSize page size
 */
public record DictionaryTypePageRequest(
        @QueryParam("input") String input,
        @QueryParam("status") BasicStatus status,
        @QueryParam("builtIn") Boolean builtIn,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public DictionaryTypePageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public DictionaryTypePageRequest() {
        this(null, null, null,
                SimpleQueryRequest.DEFAULT_PAGE_NO,
                SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

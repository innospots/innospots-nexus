package com.innospots.nexus.console.dictionary.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated dictionary item query bound from management-console query parameters.
 *
 * @param input    fuzzy item name or value
 * @param status   optional lifecycle status
 * @param pageNo   1-indexed page number
 * @param pageSize page size
 */
public record DictionaryItemPageRequest(
        @QueryParam("input") String input,
        @QueryParam("status") BasicStatus status,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public DictionaryItemPageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public DictionaryItemPageRequest() {
        this(null, null,
                SimpleQueryRequest.DEFAULT_PAGE_NO,
                SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

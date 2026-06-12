package com.innospots.nexus.base.domain.request;

/**
 * Paginated query request with a keyword filter.
 *
 * @param input    common fuzzy search keyword, may be {@code null}
 * @param pageNo   1-indexed page number, default 1
 * @param pageSize records per page, default 20
 */
public record SimpleQueryRequest(
        String input,
        long pageNo,
        long pageSize
) {

    public static final long DEFAULT_PAGE_NO = 1L;
    public static final long DEFAULT_PAGE_SIZE = 20L;

    public SimpleQueryRequest {
        if (pageNo < 1) {
            pageNo = DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
    }

    public SimpleQueryRequest() {
        this(null, DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
    }
}

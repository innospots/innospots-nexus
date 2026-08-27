package com.innospots.nexus.base.domain.request;

/**
 * Shared pagination defaults and normalization for query requests.
 */
public final class Pagination {

    public static final long DEFAULT_PAGE_NO = 1L;
    public static final long DEFAULT_PAGE_SIZE = 20L;

    private Pagination() {
    }

    /**
     * Returns {@code pageNo} when it is at least 1, otherwise {@link #DEFAULT_PAGE_NO}.
     *
     * @param pageNo requested page number
     * @return a 1-indexed page number
     */
    public static long normalizePageNo(long pageNo) {
        if (pageNo < 1) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    /**
     * Returns {@code pageSize} when it is at least 1, otherwise {@link #DEFAULT_PAGE_SIZE}.
     *
     * @param pageSize requested page size
     * @return a positive page size
     */
    public static long normalizePageSize(long pageSize) {
        if (pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
}

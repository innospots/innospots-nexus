package com.innospots.nexus.base.domain.response;

import java.util.List;

/**
 * Paginated API response wrapper. Validates page bounds at construction
 * and computes the total page count from total record count and page size.
 */
public record PageResult<T>(
        List<T> records,
        long pageNo,
        long pageSize,
        long total,
        long pages
) {

    /**
     * Compact constructor validates pagination parameters and ensures
     * the records list is never null.
     */
    public PageResult {
        if (pageNo < 1) {
            throw new IllegalArgumentException("pageNo must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize must be greater than 0");
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        records = records == null ? List.of() : List.copyOf(records);
    }

    /**
     * Creates a page result with auto-calculated total pages.
     */
    public static <T> PageResult<T> of(List<T> records, long pageNo, long pageSize, long total) {
        return new PageResult<>(records, pageNo, pageSize, total, calculatePages(total, pageSize));
    }

    /**
     * Returns an empty page result for the given page number and size.
     */
    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return of(List.of(), pageNo, pageSize, 0);
    }

    public boolean hasNext() {
        return pageNo < pages;
    }

    public boolean hasPrevious() {
        return pageNo > 1 && pages > 0;
    }

    /**
     * Calculates the total number of pages using ceiling division.
     */
    private static long calculatePages(long total, long pageSize) {
        if (total <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }
}

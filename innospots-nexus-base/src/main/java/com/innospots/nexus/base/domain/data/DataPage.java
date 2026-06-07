package com.innospots.nexus.base.domain.data;

import java.util.List;

/**
 * Immutable paginated data container. Validates page bounds at construction
 * and provides convenience methods for pagination navigation.
 */
public record DataPage<T>(
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
    public DataPage {
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
     * Creates a page with auto-calculated total pages.
     *
     * @param records  the page records (null-safe)
     * @param pageNo   1-indexed page number
     * @param pageSize number of records per page
     * @param total    total record count across all pages
     */
    public static <T> DataPage<T> of(List<T> records, long pageNo, long pageSize, long total) {
        return new DataPage<>(records, pageNo, pageSize, total, calculatePages(total, pageSize));
    }

    /**
     * Returns an empty page for the given page number and size.
     */
    public static <T> DataPage<T> empty(long pageNo, long pageSize) {
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

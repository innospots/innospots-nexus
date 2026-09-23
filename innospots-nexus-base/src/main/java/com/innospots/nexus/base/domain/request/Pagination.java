package com.innospots.nexus.base.domain.request;

/**
 * 查询请求共享的分页默认值与规范化逻辑。
 *
 * @author Smars
 * @date 2026/09/13
 * @see SimpleQueryRequest
 */
public final class Pagination {

    public static final long DEFAULT_PAGE_NO = 1L;
    public static final long DEFAULT_PAGE_SIZE = 20L;

    private Pagination() {
    }

    /**
     * 当 {@code pageNo} 至少为 1 时返回该值，否则返回 {@link #DEFAULT_PAGE_NO}。
     *
     * @param pageNo 请求的页码
     * @return 从 1 开始的页码
     */
    public static long normalizePageNo(long pageNo) {
        if (pageNo < 1) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    /**
     * 当 {@code pageSize} 至少为 1 时返回该值，否则返回 {@link #DEFAULT_PAGE_SIZE}。
     *
     * @param pageSize 请求的每页记录数
     * @return 正数的每页记录数
     */
    public static long normalizePageSize(long pageSize) {
        if (pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
}

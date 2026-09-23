package com.innospots.nexus.base.domain.request;

/**
 * 带关键词过滤的分页查询请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param input    通用模糊搜索关键词，可为 {@code null}
 * @param pageNo   从 1 开始的页码，默认 1
 * @param pageSize 每页记录数，默认 20
 * @see Pagination
 */
public record SimpleQueryRequest(
        String input,
        long pageNo,
        long pageSize
) {

    public static final long DEFAULT_PAGE_NO = Pagination.DEFAULT_PAGE_NO;
    public static final long DEFAULT_PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;

    public SimpleQueryRequest {
        pageNo = Pagination.normalizePageNo(pageNo);
        pageSize = Pagination.normalizePageSize(pageSize);
    }

    public SimpleQueryRequest() {
        this(null, DEFAULT_PAGE_NO, DEFAULT_PAGE_SIZE);
    }
}

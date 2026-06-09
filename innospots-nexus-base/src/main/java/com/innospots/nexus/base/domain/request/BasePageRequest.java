package com.innospots.nexus.base.domain.request;

/**
 * Base request object for paginated queries.
 * <p>Subclasses can add domain-specific filters while reusing common keyword
 * and pagination fields.</p>
 */
public class BasePageRequest {

    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;

    private String input;
    private Long pageNo = DEFAULT_PAGE_NO;
    private Long pageSize = DEFAULT_PAGE_SIZE;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Long getPageNo() {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    public void setPageNo(Long pageNo) {
        this.pageNo = pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    public Long getPageSize() {
        return pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }

    public void setPageSize(Long pageSize) {
        this.pageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
    }
}

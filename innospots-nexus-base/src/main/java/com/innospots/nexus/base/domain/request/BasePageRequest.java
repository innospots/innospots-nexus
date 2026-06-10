package com.innospots.nexus.base.domain.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Base request object for paginated queries.
 * <p>Subclasses can add domain-specific filters while reusing common keyword
 * and pagination fields.</p>
 */
@Getter
@Setter
public class BasePageRequest {

    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;

    private String input;
    private Long pageNo = DEFAULT_PAGE_NO;
    private Long pageSize = DEFAULT_PAGE_SIZE;

}

package com.innospots.nexus.kernel.user.domain.request;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * Paginated user query request.
 *
 * @param input    common fuzzy search keyword
 * @param pageNo   1-indexed page number, default 1
 * @param pageSize page size, default 20
 * @param userName user name filter
 * @param realName real name filter
 * @param email    email filter
 * @param mobile   mobile number filter
 */
public record UserPageRequest(
        String input,
        long pageNo,
        long pageSize,
        String userName,
        String realName,
        String email,
        String mobile
) {

    public UserPageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public UserPageRequest() {
        this(null, SimpleQueryRequest.DEFAULT_PAGE_NO, SimpleQueryRequest.DEFAULT_PAGE_SIZE,
                null, null, null, null);
    }
}

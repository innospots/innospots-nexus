package com.innospots.nexus.portal.user.domain.request;

import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * 分页租户用户查询请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param input       通用模糊搜索关键字
 * @param pageNo      从 1 开始的页码，默认 1
 * @param pageSize    分页大小，默认 20
 * @param userName    用户名过滤条件
 * @param displayName 显示名称 filter
 * @param email       邮箱过滤条件
 * @param mobile      手机号过滤条件
 */
public record UserPageRequest(
        String input,
        long pageNo,
        long pageSize,
        String userName,
        String displayName,
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

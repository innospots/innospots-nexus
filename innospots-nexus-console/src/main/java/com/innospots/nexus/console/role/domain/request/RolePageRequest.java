package com.innospots.nexus.console.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.SimpleQueryRequest;

/**
 * 由管理控制台查询参数绑定的分页角色查询。
 *
 * @author Smars
 * @date 2026/09/13
 * @param input    角色名称或编码的模糊匹配
 * @param status   可选 生命周期状态
 * @param builtIn  可选 built-in role filter
 * @param pageNo   从 1 开始的页码
 * @param pageSize 分页大小
 */
public record RolePageRequest(
        @QueryParam("input") String input,
        @QueryParam("status") BasicStatus status,
        @QueryParam("builtIn") Boolean builtIn,
        @DefaultValue("1") @QueryParam("pageNo") long pageNo,
        @DefaultValue("20") @QueryParam("pageSize") long pageSize
) {

    public RolePageRequest {
        if (pageNo < 1) {
            pageNo = SimpleQueryRequest.DEFAULT_PAGE_NO;
        }
        if (pageSize < 1) {
            pageSize = SimpleQueryRequest.DEFAULT_PAGE_SIZE;
        }
    }

    public RolePageRequest() {
        this(null, null, null,
                SimpleQueryRequest.DEFAULT_PAGE_NO,
                SimpleQueryRequest.DEFAULT_PAGE_SIZE);
    }
}

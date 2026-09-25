package com.innospots.nexus.console.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * 绑定到角色的主体的分页查询。
 *
 * <p>使用 Bean 类而非 record，以兼容 Quarkus REST 对 {@code @BeanParam} 的注入代码生成。</p>
 */
public final class RoleBindingPageRequest {

    @QueryParam("input")
    private String input;

    @QueryParam("subjectType")
    private RoleBindingSubjectType subjectType;

    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public RoleBindingPageRequest() {
    }

    public String input() {
        return input;
    }

    public RoleBindingSubjectType subjectType() {
        return subjectType;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

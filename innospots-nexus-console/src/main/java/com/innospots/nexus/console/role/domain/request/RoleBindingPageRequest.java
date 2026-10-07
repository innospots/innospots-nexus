package com.innospots.nexus.console.role.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.console.role.domain.enums.RoleBindingSubjectType;

/**
 * 绑定到角色的主体的分页查询。
 *
 * <p>使用 Bean 类而非 record，以兼容 Quarkus REST 对 {@code @BeanParam} 的注入代码生成。</p>
 *
 * @param input       主体标识符或名称的模糊匹配
 * @param subjectType 可选主体类型
 * @param pageNo      从 1 开始的页码，默认 1
 * @param pageSize    分页大小，默认 20
 */
@Schema(name = "RoleBindingPageRequest", description = "分页角色绑定查询")
public final class RoleBindingPageRequest {

    @Schema(description = "主体标识符或名称的模糊匹配")
    @QueryParam("input")
    private String input;

    @Schema(description = "可选主体类型")
    @QueryParam("subjectType")
    private RoleBindingSubjectType subjectType;

    @Schema(description = "从 1 开始的页码，默认 1")
    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @Schema(description = "分页大小，默认 20")
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

package com.innospots.nexus.console.dictionary.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.Pagination;

/**
 * 由管理控制台查询参数绑定的分页字典类型查询。
 *
 * <p>使用 Bean 类而非 record，以兼容 Quarkus REST 对 {@code @BeanParam} 的注入代码生成。</p>
 */
public final class DictionaryTypePageRequest {

    @QueryParam("input")
    private String input;

    @QueryParam("status")
    private BasicStatus status;

    @QueryParam("builtIn")
    private Boolean builtIn;

    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public DictionaryTypePageRequest() {
    }

    public String input() {
        return input;
    }

    public BasicStatus status() {
        return status;
    }

    public Boolean builtIn() {
        return builtIn;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

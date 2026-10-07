package com.innospots.nexus.console.dictionary.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.base.domain.request.Pagination;

/**
 * 由管理控制台查询参数绑定的分页字典项查询。
 *
 * <p>使用 Bean 类而非 record，以兼容 Quarkus REST 对 {@code @BeanParam} 的注入代码生成。</p>
 *
 * @param input    字典项名称或值的模糊匹配
 * @param status   可选生命周期状态
 * @param pageNo   从 1 开始的页码，默认 1
 * @param pageSize 分页大小，默认 20
 */
@Schema(name = "DictionaryItemPageRequest", description = "分页字典项查询")
public final class DictionaryItemPageRequest {

    @Schema(description = "字典项名称或值的模糊匹配")
    @QueryParam("input")
    private String input;

    @Schema(description = "可选生命周期状态")
    @QueryParam("status")
    private BasicStatus status;

    @Schema(description = "从 1 开始的页码，默认 1")
    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @Schema(description = "分页大小，默认 20")
    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public DictionaryItemPageRequest() {
    }

    public String input() {
        return input;
    }

    public BasicStatus status() {
        return status;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

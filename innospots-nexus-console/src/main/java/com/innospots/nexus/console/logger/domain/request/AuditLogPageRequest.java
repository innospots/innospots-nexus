package com.innospots.nexus.console.logger.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 分页审计日志查询（与控制台 UI 查询参数对齐）。
 */
@Schema(name = "AuditLogPageRequest", description = "分页审计日志查询")
public final class AuditLogPageRequest {

    @Schema(description = "时间范围：last24h、last7d、last30d")
    @DefaultValue("last7d")
    @QueryParam("timeRange")
    private String timeRange;

    @Schema(description = "操作者模糊匹配")
    @QueryParam("operator")
    private String operator;

    @Schema(description = "操作类型，如 CREATE、UPDATE")
    @QueryParam("operationType")
    private String operationType;

    @Schema(description = "执行结果：SUCCESS 或 FAILURE")
    @QueryParam("result")
    private String result;

    @Schema(description = "路径、动作或消息关键字")
    @QueryParam("keyword")
    private String keyword;

    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public AuditLogPageRequest() {
    }

    public String timeRange() {
        return timeRange;
    }

    public String operator() {
        return operator;
    }

    public String operationType() {
        return operationType;
    }

    public String result() {
        return result;
    }

    public String keyword() {
        return keyword;
    }

    public long pageNo() {
        return pageNo;
    }

    public long pageSize() {
        return pageSize;
    }

}

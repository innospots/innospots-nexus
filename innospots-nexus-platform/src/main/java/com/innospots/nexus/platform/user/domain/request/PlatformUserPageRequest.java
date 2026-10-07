package com.innospots.nexus.platform.user.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.platform.user.domain.enums.PlatformUserStatus;

/**
 * 平台用户分页查询参数。
 */
@Schema(name = "PlatformUserPageRequest", description = "分页平台用户查询")
public final class PlatformUserPageRequest {

    @Schema(description = "登录名或显示名称的模糊匹配")
    @QueryParam("input")
    private String input;

    @Schema(description = "可选生命周期状态")
    @QueryParam("status")
    private PlatformUserStatus status;

    @Schema(description = "从 1 开始的页码，默认 1")
    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @Schema(description = "分页大小，默认 20")
    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public PlatformUserPageRequest() {
    }

    public String input() {
        return input;
    }

    public PlatformUserStatus status() {
        return status;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

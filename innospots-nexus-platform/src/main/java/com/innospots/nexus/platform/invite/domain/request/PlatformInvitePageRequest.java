package com.innospots.nexus.platform.invite.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.platform.invite.domain.enums.PlatformInviteStatus;

@Schema(name = "PlatformInvitePageRequest", description = "分页邀请查询")
public final class PlatformInvitePageRequest {

    @QueryParam("input")
    private String input;

    @QueryParam("status")
    private PlatformInviteStatus status;

    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public PlatformInvitePageRequest() {
    }

    public String input() {
        return input;
    }

    public PlatformInviteStatus status() {
        return status;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

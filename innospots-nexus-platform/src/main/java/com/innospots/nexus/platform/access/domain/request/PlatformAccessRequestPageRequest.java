package com.innospots.nexus.platform.access.domain.request;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.innospots.nexus.base.domain.request.Pagination;
import com.innospots.nexus.platform.access.domain.enums.PlatformAccessRequestStatus;

@Schema(name = "PlatformAccessRequestPageRequest", description = "分页访问申请查询")
public final class PlatformAccessRequestPageRequest {

    @QueryParam("status")
    private PlatformAccessRequestStatus status;

    @DefaultValue("1")
    @QueryParam("pageNo")
    private long pageNo;

    @DefaultValue("20")
    @QueryParam("pageSize")
    private long pageSize;

    public PlatformAccessRequestPageRequest() {
    }

    public PlatformAccessRequestStatus status() {
        return status;
    }

    public long pageNo() {
        return Pagination.normalizePageNo(pageNo);
    }

    public long pageSize() {
        return Pagination.normalizePageSize(pageSize);
    }
}

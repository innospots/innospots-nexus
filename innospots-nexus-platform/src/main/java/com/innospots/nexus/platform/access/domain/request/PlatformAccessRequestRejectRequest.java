package com.innospots.nexus.platform.access.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformAccessRequestRejectRequest", description = "拒绝访问申请")
public record PlatformAccessRequestRejectRequest(
        @Schema(description = "拒绝原因")
        String rejectReason
) {
}

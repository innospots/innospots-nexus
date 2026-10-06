package com.innospots.nexus.platform.access.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformAccessRequestApproveRequest", description = "通过注册待审核申请")
public record PlatformAccessRequestApproveRequest(
        @Schema(description = "默认角色编码，逗号分隔（预留）")
        String defaultRoleCodes
) {
}

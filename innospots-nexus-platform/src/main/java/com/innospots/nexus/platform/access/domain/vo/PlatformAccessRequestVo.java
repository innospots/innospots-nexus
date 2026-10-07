package com.innospots.nexus.platform.access.domain.vo;

import java.time.LocalDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "PlatformAccessRequestVo", description = "平台注册待审核申请")
public record PlatformAccessRequestVo(
        @Schema(description = "申请 ID", required = true)
        String accessRequestId,
        @Schema(description = "登录名")
        String loginName,
        @Schema(description = "平台用户 ID")
        String platformUserId,
        @Schema(description = "申请人姓名")
        String applicantName,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "手机")
        String mobile,
        @Schema(description = "申请说明")
        String description,
        @Schema(description = "状态")
        String status,
        @Schema(description = "拒绝原因")
        String rejectReason,
        @Schema(description = "审批时间")
        LocalDateTime reviewedAt,
        @Schema(description = "创建时间")
        LocalDateTime createdAt
) {
}

package com.innospots.nexus.console.logger.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 审计日志展示模型（列表与详情共用）。
 */
@Schema(name = "AuditLogVo", description = "审计日志")
public record AuditLogVo(
        @Schema(description = "日志 ID") String logId,
        @Schema(description = "操作时间") String occurredAt,
        @Schema(description = "操作者显示名") String operatorName,
        @Schema(description = "操作者 ID") String operatorId,
        @Schema(description = "安全域") String securityRealm,
        @Schema(description = "操作类型（EndpointGrant.action）") String operationType,
        @Schema(description = "操作对象（EndpointGrant.name）") String target,
        @Schema(description = "执行方法（HTTP 方法 + 路径）") String method,
        @Schema(description = "结果 SUCCESS/FAILURE") String result,
        @Schema(description = "耗时毫秒，无采集时为 null") Long durationMs,
        @Schema(description = "HTTP 或业务状态码") String errorCode,
        @Schema(description = "操作说明（EndpointGrant.label）") String summary,
        @Schema(description = "失败详情，成功时为 null") String failureDetail,
        @Schema(description = "请求参数 JSON") String requestPayload
) {
}

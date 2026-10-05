package com.innospots.nexus.platform.tenant.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 开通租户及其企业法定档案的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantName         显示名称
 * @param tenantCode         唯一租户编码
 * @param tenantType         租户类型（{@link com.innospots.nexus.platform.tenant.domain.enums.TenantType}）
 * @param planCode           可选 plan reference
 * @param ownerTenantUserId  可选 initial tenant-user owner
 * @param legalName          企业法定名称
 * @param creditCode         统一社会信用代码
 * @param industry           行业分类
 * @param contactName        主联系人姓名
 * @param contactPhone       主联系人电话
 * @param contactEmail       主联系人邮箱
 * @param address            注册地址
 */
@Schema(name = "TenantCreateRequest", description = "开通租户及其企业法定档案的请求")
public record TenantCreateRequest(
        @Schema(description = "租户显示名称", required = true, examples = {"示例租户"})
        String tenantName,
        @Schema(description = "唯一租户编码", required = true, examples = {"demo-tenant"})
        String tenantCode,
        @Schema(description = "租户类型", required = true, examples = {"TEAM"})
        String tenantType,
        @Schema(description = "套餐/计划编码")
        String planCode,
        @Schema(description = "初始租户用户 owner ID")
        String ownerTenantUserId,
        @Schema(description = "企业法定名称", required = true)
        String legalName,
        @Schema(description = "统一社会信用代码")
        String creditCode,
        @Schema(description = "行业分类")
        String industry,
        @Schema(description = "主联系人姓名")
        String contactName,
        @Schema(description = "主联系人电话")
        String contactPhone,
        @Schema(description = "主联系人邮箱", format = "email")
        String contactEmail,
        @Schema(description = "注册地址")
        String address
) {
}

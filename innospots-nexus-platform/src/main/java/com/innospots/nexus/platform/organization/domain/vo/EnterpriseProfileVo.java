package com.innospots.nexus.platform.organization.domain.vo;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 企业法定档案 REST 响应概要。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.domain.entity.EnterpriseEntity
 * @param enterpriseId 企业档案主键
 * @param tenantId     所属租户
 * @param legalName    法定名称
 * @param creditCode   统一社会信用代码
 * @param industry     行业
 * @param contactName  联系人
 * @param contactPhone 联系电话
 * @param contactEmail 联系邮箱
 * @param address      地址
 * @param extra        扩展信息
 */
@Schema(name = "EnterpriseProfileVo", description = "企业法定档案")
public record EnterpriseProfileVo(
        @Schema(description = "企业档案 ID", required = true)
        String enterpriseId,
        @Schema(description = "所属租户 ID", required = true)
        String tenantId,
        @Schema(description = "法定名称", required = true)
        String legalName,
        @Schema(description = "统一社会信用代码")
        String creditCode,
        @Schema(description = "行业分类")
        String industry,
        @Schema(description = "主联系人姓名")
        String contactName,
        @Schema(description = "主联系人电话")
        String contactPhone,
        @Schema(description = "主联系人邮箱")
        String contactEmail,
        @Schema(description = "地址")
        String address,
        @Schema(description = "扩展属性")
        String extra
) {
}

package com.innospots.nexus.platform.organization.domain.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * 创建或更新租户下企业法定档案的 REST 请求体。
 *
 * @author Smars
 * @date 2026/10/06
 * @see com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint#upsertEnterpriseProfile
 * @param legalName    法定名称
 * @param creditCode   统一社会信用代码
 * @param industry     行业
 * @param contactName  联系人
 * @param contactPhone 联系电话
 * @param contactEmail 联系邮箱
 * @param address      地址
 * @param extra        扩展信息
 */
@Schema(name = "EnterpriseProfileUpsertRequest", description = "企业法定档案")
public record EnterpriseProfileUpsertRequest(
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
        @Schema(description = "注册或通讯地址")
        String address,
        @Schema(description = "扩展属性")
        String extra
) {
}

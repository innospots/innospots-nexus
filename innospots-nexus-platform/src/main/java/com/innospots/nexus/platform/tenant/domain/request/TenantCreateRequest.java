package com.innospots.nexus.platform.tenant.domain.request;

/**
 * 开通租户及其企业法定档案的请求。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantName         显示名称
 * @param tenantCode         唯一租户编码
 * @param planCode           可选 plan reference
 * @param ownerTenantUserId  可选 initial tenant-user owner
 * @param legalName          企业法定名称
 * @param creditCode         统一社会信用代码
 * @param industry           行业分类
 * @param contactName        运维联系人姓名
 * @param contactPhone       运维联系人电话
 * @param contactEmail       运维联系人邮箱
 * @param address            注册地址
 */
public record TenantCreateRequest(
        String tenantName,
        String tenantCode,
        String planCode,
        String ownerTenantUserId,
        String legalName,
        String creditCode,
        String industry,
        String contactName,
        String contactPhone,
        String contactEmail,
        String address
) {
}

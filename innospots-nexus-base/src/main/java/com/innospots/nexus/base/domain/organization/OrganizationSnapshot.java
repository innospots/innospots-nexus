package com.innospots.nexus.base.domain.organization;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * 租户面向业务的档案（语言环境、货币、品牌标识）。
 * 这不是 {@code nx_organization_unit}；内部组织树保留在 kernel 中。
 *
 * @author Smars
 * @date 2026/09/13
 * @param tenantId         租户 ID
 * @param organizationCode 组织编码
 * @param organizationName 组织名称
 * @param defaultLocale    默认语言环境
 * @param defaultCurrency  默认货币
 * @param logoKey          Logo 资源键
 * @param status           状态
 * @see com.innospots.nexus.base.domain.tenant.TenantSnapshot
 */
public record OrganizationSnapshot(
        String tenantId,
        String organizationCode,
        String organizationName,
        String defaultLocale,
        String defaultCurrency,
        String logoKey,
        BasicStatus status
) {
}

package com.innospots.nexus.base.domain.organization;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * Business-facing profile of a tenant (locale, currency, branding).
 * This is not {@code nx_organization_unit}; internal org trees stay in kernel.
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

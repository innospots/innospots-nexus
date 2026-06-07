package com.innospots.nexus.base.domain.organization;

import com.innospots.nexus.base.domain.enums.BasicStatus;

/**
 * An organization/tenant with locale, currency, and branding preferences.
 */
public record OrganizationInfo(
        Long organizationId,
        String organizationName,
        String organizationCode,
        String defaultLocale,
        String defaultCurrency,
        String logo,
        BasicStatus status
) {
}

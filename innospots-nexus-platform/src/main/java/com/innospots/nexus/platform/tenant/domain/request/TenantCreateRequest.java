package com.innospots.nexus.platform.tenant.domain.request;

/**
 * Request to open a tenant together with its enterprise legal profile.
 *
 * @param tenantName         display name
 * @param tenantCode         unique tenant code
 * @param planCode           optional plan reference
 * @param ownerTenantUserId  optional initial tenant-user owner
 * @param legalName          enterprise legal name
 * @param creditCode         unified social credit code
 * @param industry           industry classification
 * @param contactName        ops contact name
 * @param contactPhone       ops contact phone
 * @param contactEmail       ops contact email
 * @param address            registered address
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

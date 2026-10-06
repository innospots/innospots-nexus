package com.innospots.nexus.quarkus.platform.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import com.innospots.nexus.platform.organization.dao.EnterpriseDao;
import com.innospots.nexus.platform.organization.dao.TenantDao;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;
import com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService;
import com.innospots.nexus.platform.organization.service.PlatformTenantService;

/**
 * 运营管理平台组织域 Quarkus CDI 装配。
 */
@ApplicationScoped
public class PlatformOrganizationBeans {

    @Produces
    @Singleton
    TenantOperator tenantOperator(TenantDao tenantDao) {
        return new TenantOperator(tenantDao);
    }

    @Produces
    @Singleton
    EnterpriseOperator enterpriseOperator(EnterpriseDao enterpriseDao) {
        return new EnterpriseOperator(enterpriseDao);
    }

    @Produces
    @Singleton
    PlatformTenantService platformTenantService(
            TenantOperator tenantOperator,
            EnterpriseOperator enterpriseOperator) {
        return new PlatformTenantService(tenantOperator, enterpriseOperator);
    }

    @Produces
    @Singleton
    PlatformEnterpriseProfileService platformEnterpriseProfileService(
            TenantOperator tenantOperator,
            EnterpriseOperator enterpriseOperator) {
        return new PlatformEnterpriseProfileService(tenantOperator, enterpriseOperator);
    }
}

package com.innospots.nexus.spring.platform.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.platform.organization.dao.EnterpriseDao;
import com.innospots.nexus.platform.organization.dao.TenantDao;
import com.innospots.nexus.platform.organization.endpoint.TenantEndpoint;
import com.innospots.nexus.platform.organization.endpoint.EnterpriseProfileEndpoint;
import com.innospots.nexus.platform.organization.operator.EnterpriseOperator;
import com.innospots.nexus.platform.organization.operator.TenantOperator;
import com.innospots.nexus.platform.organization.service.PlatformEnterpriseProfileService;
import com.innospots.nexus.platform.organization.service.PlatformTenantService;

/**
 * 运营管理平台组织域 Spring 装配。
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.platform.organization.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class PlatformOrganizationConfiguration {

    @Bean
    TenantOperator tenantOperator(TenantDao tenantDao) {
        return new TenantOperator(tenantDao);
    }

    @Bean
    EnterpriseOperator enterpriseOperator(EnterpriseDao enterpriseDao) {
        return new EnterpriseOperator(enterpriseDao);
    }

    @Bean
    PlatformTenantService platformTenantService(
            TenantOperator tenantOperator,
            EnterpriseOperator enterpriseOperator) {
        return new PlatformTenantService(tenantOperator, enterpriseOperator);
    }

    @Bean
    PlatformEnterpriseProfileService platformEnterpriseProfileService(
            TenantOperator tenantOperator,
            EnterpriseOperator enterpriseOperator) {
        return new PlatformEnterpriseProfileService(tenantOperator, enterpriseOperator);
    }

    @Bean
    @Lazy
    TenantEndpoint tenantEndpoint(PlatformTenantService platformTenantService) {
        return new TenantEndpoint(platformTenantService);
    }

    @Bean
    @Lazy
    EnterpriseProfileEndpoint enterpriseProfileEndpoint(
            PlatformEnterpriseProfileService platformEnterpriseProfileService) {
        return new EnterpriseProfileEndpoint(platformEnterpriseProfileService);
    }
}

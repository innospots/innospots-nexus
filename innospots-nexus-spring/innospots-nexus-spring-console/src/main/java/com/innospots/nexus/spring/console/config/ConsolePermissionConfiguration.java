package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.RequestAuthorizer;
import com.innospots.nexus.console.permission.authorization.SessionAuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.endpoint.CurrentAuthorizationEndpoint;
import com.innospots.nexus.console.permission.endpoint.GrantManagementEndpoint;
import com.innospots.nexus.console.permission.service.GrantSubjectAccess;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;

/**
 * {@code console.permission} 域 Spring 装配：授权主体解析、请求鉴权与授权管理 REST。
 *
 * @author Smars
 * @date 2026/09/23
 * @see RequestAuthorizer
 */
@Configuration
@MapperScan(
        basePackages = "com.innospots.nexus.console.permission.dao",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "sqlSessionFactory")
public class ConsolePermissionConfiguration {

    @Bean
    PermissionVisibilityService permissionVisibilityService(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new PermissionVisibilityService(resourceDao, grantDao);
    }

    @Bean
    GrantSubjectAccess grantSubjectAccess(RoleDao roleDao) {
        return new GrantSubjectAccess(roleDao, null);
    }

    @Bean
    PermissionGrantService permissionGrantService(
            PermissionGrantDao grantDao,
            ConsoleCatalogResourceDao resourceDao,
            GrantSubjectAccess grantSubjectAccess) {
        return new PermissionGrantService(grantDao, resourceDao, grantSubjectAccess);
    }

    @Bean
    AuthorizationSubjectResolver authorizationSubjectResolver(
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new SessionAuthorizationSubjectResolver(roleBindingDao, roleDao);
    }

    @Bean
    RequestAuthorizer requestAuthorizer(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new RequestAuthorizer(resourceDao, grantDao);
    }

    @Bean
    @Lazy
    GrantManagementEndpoint grantManagementEndpoint(PermissionGrantService permissionGrantService) {
        return new GrantManagementEndpoint(permissionGrantService);
    }

    @Bean
    @Lazy
    CurrentAuthorizationEndpoint currentAuthorizationEndpoint(
            AuthorizationSubjectResolver authorizationSubjectResolver,
            PermissionVisibilityService permissionVisibilityService) {
        return new CurrentAuthorizationEndpoint(authorizationSubjectResolver, permissionVisibilityService);
    }
}

package com.innospots.nexus.spring.console.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.innospots.nexus.console.catalog.dao.ConsoleCatalogResourceDao;
import com.innospots.nexus.console.catalog.service.ConsoleCatalogService;
import com.innospots.nexus.console.permission.authorization.AuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.authorization.ConsolePagePermissionAuthorizer;
import com.innospots.nexus.console.permission.authorization.SessionAuthorizationSubjectResolver;
import com.innospots.nexus.console.permission.dao.PermissionGrantDao;
import com.innospots.nexus.console.permission.service.GrantSubjectAccess;
import com.innospots.nexus.console.permission.pageaccess.GrantGroupPageAccessVerifier;
import com.innospots.nexus.console.permission.pageaccess.PageAccessVerifier;
import com.innospots.nexus.console.permission.service.PermissionGrantService;
import com.innospots.nexus.console.permission.service.PermissionVisibilityService;
import com.innospots.nexus.console.role.dao.RoleBindingDao;
import com.innospots.nexus.console.role.dao.RoleDao;

/**
 * {@code console.permission} 域 Spring 装配：授权主体解析、请求鉴权与授权核心服务（REST 由 platform 实现）。
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
            ConsoleCatalogService catalogService,
            GrantSubjectAccess grantSubjectAccess) {
        return new PermissionGrantService(grantDao, catalogService, grantSubjectAccess);
    }

    @Bean
    AuthorizationSubjectResolver authorizationSubjectResolver(
            RoleBindingDao roleBindingDao,
            RoleDao roleDao) {
        return new SessionAuthorizationSubjectResolver(roleBindingDao, roleDao);
    }

    @Bean
    ConsolePagePermissionAuthorizer consolePagePermissionAuthorizer(
            ConsoleCatalogResourceDao resourceDao,
            PermissionGrantDao grantDao) {
        return new ConsolePagePermissionAuthorizer(resourceDao, grantDao);
    }

    @Bean
    PageAccessVerifier pageAccessVerifier(ConsolePagePermissionAuthorizer pagePermissionAuthorizer) {
        return new GrantGroupPageAccessVerifier(pagePermissionAuthorizer);
    }
}
